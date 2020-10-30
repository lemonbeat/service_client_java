package com.lemonbeat;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Hdr;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl.MessageType;
import com.lemonbeat.lsbl.lsbl_common.CommonResponse;
import com.lemonbeat.lsbl.lsbl_common_base_types.NackResponse;
import com.rabbitmq.client.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class provides the general communication with the backend.
 * It offers various callbacks for event and RPC mechanisms.
 *
 * @author Lemonbeat GmbH
 * @version 4.0.0
 */
public class ServiceClient {

    private Connection connection;
    private String token;
    private long tokenExpires;

    private static String REPLY_QUEUE_PREFIX = "PARTNER.CLIENT.";
    private static String EVENTS_QUEUE_PREFIX = "PARTNER.EVENTS.";
    private static String DMZ_EXCHANGE = "DMZ";
    private static String RPC_EXCHANGE = "PARTNER";
    private static String EVENT_EXCHANGE = "EVENT.APP";
    private static String CLIENT_NAME = "CLIENT";

    /**
     * Create a new instance by passing a RabbitMQ connection object.
     * @param connection RabbitMQ connection instance
     */
    public ServiceClient(Connection connection){
        this.connection = connection;
    }

    /**
     * Create a new instance by passing the path to .properties file with the settings.
     * @param propertiesFile
     */
    public ServiceClient(String propertiesFile){
        try {
            Properties settings = new Properties();
            settings.load(new FileInputStream(propertiesFile));

            int broker_port = Integer.parseInt(settings.getProperty("BROKER_PORT", "5671"));
            String broker_host = settings.getProperty("BROKER_HOST", "localhost");
            String broker_vhost = settings.getProperty("BROKER_VHOST", "/");
            String broker_username = settings.getProperty("BROKER_USERNAME", "guest");
            String broker_password = settings.getProperty("BROKER_PASSWORD", "guest");
            String truststore_path = settings.getProperty("TRUSTSTORE_PATH", "keystore.jks");
            String truststore_pass = settings.getProperty("TRUSTSTORE_PASSWORD", "password");
            String client_p12_path = settings.getProperty("CLIENT_P12_PATH", "client.p12");
            String client_p12_pass = settings.getProperty("CLIENT_P12_PASSWORD", "password");
            ServiceClient.CLIENT_NAME = settings.getProperty("CLIENT_NAME", "client.p12");

            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(client_p12_path), client_p12_pass.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, client_p12_pass.toCharArray());

            KeyStore tks = KeyStore.getInstance("JKS");
            tks.load(new FileInputStream(truststore_path), truststore_pass.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(tks);

            SSLContext c = SSLContext.getInstance("TLSv1.2");
            c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(broker_host);
            factory.setVirtualHost(broker_vhost);
            factory.setUsername(broker_username);
            factory.setPassword(broker_password);
            factory.setPort(broker_port);
            factory.useSslProtocol(c);
            connection = factory.newConnection();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Use this to subscribe your callback to certain events. The queue is deleted when the consumer disconnects.
     * @param eventName Name of the event, e.g. EVENT.APP.TOPOSERVICE.DEVICE_INCLUDED
     * @param callback EventCallback with an onEvent method
     */
    public String subscribe(String eventName, EventCallback callback) {
        return subscribe(eventName, callback, false);
    }

    /**
     * Use this to subscribe your callback to certain events.
     * @param eventName Name of the event, e.g. EVENT.APP.TOPOSERVICE.DEVICE_INCLUDED
     * @param callback EventCallback with an onEvent method
     * @param durable Creates a queue that will be persistet if durable is set to true.
     *                Allows events to be stored on the broker even if the consumer is not connected.
     */
    public String subscribe(String eventName, EventCallback callback, boolean durable){
        try {

            final Channel channel = connection.createChannel();
            final String queueName = eventQueueName(eventName, durable);
            String eventsQueueName;

            if(durable){
                eventsQueueName = channel.queueDeclare(queueName, true, false, false, null).getQueue();
            } else {
                eventsQueueName = channel.queueDeclare(queueName, false, true, true, null).getQueue();
            }
            // Start with a prefetch count of 1 to ensure the client is not overloaded
            // You can tune this value based on your resources: https://www.rabbitmq.com/consumer-prefetch.html
            channel.basicQos(1);
            channel.queueBind(eventsQueueName, ServiceClient.EVENT_EXCHANGE, eventName);

            String consumerTag = channel.basicConsume(eventsQueueName, false, new DefaultConsumer(channel){

                private boolean reconnection = false;
                private Channel currentChannel = channel;

                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    Lsbl event = null;
                    try {
                        String lsblXML = autoRemoveBom(new String(body, StandardCharsets.UTF_8.name()));
                        event = LsBL.parse(lsblXML);
                    } catch (JAXBException e) {
                        e.printStackTrace();
                    } finally {
                        callback.onEvent(event);
                        this.currentChannel.basicAck(envelope.getDeliveryTag(), true);
                    }
                }

                @Override
                public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
                    this.reconnection = true;
                    super.handleShutdownSignal(consumerTag, sig);
                }

                @Override
                public void handleConsumeOk(String consumerTag) {
                    if(reconnection) {
                        try {
                            String eventsQueueName;
                            if(durable){
                                eventsQueueName = channel.queueDeclare(queueName, true, false, false, null).getQueue();
                            } else {
                                eventsQueueName = channel.queueDeclare(queueName, false, true, true, null).getQueue();
                            }
                            this.currentChannel.basicQos(1);
                            this.currentChannel.queueBind(eventsQueueName, ServiceClient.EVENT_EXCHANGE, eventName);
                            this.reconnection = false;
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                    super.handleConsumeOk(consumerTag);
                }

            });
            return consumerTag;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Use this function to do RPC calls to the backend services.
     * @param request Command that is sent to the Services
     * @param callback ReponseCallback with an onResponse method
     */
    public void call(Lsbl request, ResponseCallback callback) {
        AtomicBoolean responseReceived = new AtomicBoolean(false);
        try{
            Channel channel = connection.createChannel();
            String replyQueueName = channel.queueDeclare(randomReplyQueueName(), false, true, true, null).getQueue();
            channel.queueBind(replyQueueName, ServiceClient.RPC_EXCHANGE, replyQueueName);
            request.getAdr().setSrc(replyQueueName);
            request.getAdr().setSeq(ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
            Hdr hdr = new Hdr();
            hdr.setToken(this.token);
            request.setHdr(hdr);
            channel.basicPublish(ServiceClient.DMZ_EXCHANGE, request.getAdr().getTarget(), null, LsBL.write(request).getBytes("UTF-8"));
            String consumerTag = channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    Lsbl response = null;
                    try {
                        String lsblXML = autoRemoveBom(new String(body, StandardCharsets.UTF_8.name()));
                        response = LsBL.parse(lsblXML);
                    } catch (JAXBException e) {
                        e.printStackTrace();
                    } finally {
                        responseReceived.set(true);
                        callback.onResponse(response);
                    }
                }
            });

            Thread waitForResponse = new Thread(() -> {
                int timeout = 120000;
                try {
                    while(!responseReceived.get() && timeout > 0) {
                        Thread.sleep(500);
                        timeout = timeout - 500;
                    }
                    if(timeout <= 0 && !responseReceived.get()){
                        callback.onResponse(createTimeout(request));
                    } else {
                    }
                    channel.basicCancel(consumerTag);
                    channel.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            waitForResponse.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Use this function to do blocking RPC calls to the backend services.
     * @param request Command that is sent to the Services
     * @return Lsbl response for the given request.
     */
    public Lsbl callAwait(Lsbl request) {
        AtomicBoolean responseReceived = new AtomicBoolean(false);
        final Lsbl[] response = new Lsbl[1];

        this.call(request, receivedRespone -> {
            responseReceived.set(true);
            response[0] = receivedRespone;
        });

        while(!responseReceived.get()){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return response[0];
    }

    /**
     * Returns the currently used JWT
     * @return Current token or null if not authenticated
     */
    public String getToken() {
        return token;
    }


    /**
     * Sets the JWT that will be used for all subsequent messages
     * @param token JWT
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Returns the UTC Unix timestamp of the expiry of the token
     * @return Current expiriy timestamp
     */
    public long getTokenExpires() {
        return tokenExpires;
    }

    /**
     * Returns the current AMQP connection
     * @return Connection or null if no connection is available
     */
    public Connection getConnection(){
        return connection;
    }

    /**
     * Sets the UTC Unix timestamp of the token expiry
     * @param tokenExpires UTC Unix Timestamp
     */
    public void setTokenExpires(long tokenExpires) {
        this.tokenExpires = tokenExpires;
    }

    private Lsbl createTimeout(Lsbl request) {
        Lsbl lsbl = new Lsbl();
        Lsbl.Adr adr = new Lsbl.Adr();
        adr.setSeq(request.getAdr().getSeq());
        adr.setSrc(request.getAdr().getTarget());
        adr.setTarget(request.getAdr().getSrc());
        adr.setType(MessageType.LSBL_APP_NACK);
        Lsbl.Response response = new Lsbl.Response();
        CommonResponse commonResponse = new CommonResponse();
        NackResponse nackResponse = new NackResponse();
        nackResponse.setErrorCode("timeout");
        nackResponse.setMessage("The request timed out");
        nackResponse.setTimestamp(BigInteger.valueOf(System.currentTimeMillis() / 1000));
        commonResponse.setNack(nackResponse);
        response.setCommonResponse(commonResponse);
        lsbl.setResponse(response);
        lsbl.setAdr(adr);
        return lsbl;
    }

    private String eventQueueName(String eventName, boolean durable) {
        String suffix = eventName.replace("EVENT.APP", "");
        if(durable){
            return ServiceClient.EVENTS_QUEUE_PREFIX + ServiceClient.CLIENT_NAME.toUpperCase() + suffix;

        } else {
            int replyQueueNum = ThreadLocalRandom.current().nextInt(1000000, 9999999);
            return ServiceClient.EVENTS_QUEUE_PREFIX + ServiceClient.CLIENT_NAME.toUpperCase() + suffix + "." +System.currentTimeMillis()+replyQueueNum;
        }
    }

    private String randomReplyQueueName() {
        int replyQueueNum = ThreadLocalRandom.current().nextInt(1000000, 9999999);
        return ServiceClient.REPLY_QUEUE_PREFIX + ServiceClient.CLIENT_NAME.toUpperCase() + "." + System.currentTimeMillis()+replyQueueNum;
    }

    /**
     * Some messages might contain a UTF-Byte Order Mark.
     * This might fail in parsing, this function automatically removes this.
     * @param s String that might contain a BOM
     * @return String without a BOM
     */
    private String autoRemoveBom(String s){
        String UTF8_BOM = "\uFEFF";
        if(s.startsWith(UTF8_BOM)){
            s = s.substring(1);
        }
        return s;
    }

    public interface EventCallback {
        void onEvent(Lsbl event);
    }

    public interface ResponseCallback {
        void onResponse(Lsbl response);
    }

}
