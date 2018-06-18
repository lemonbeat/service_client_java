package com.lemonbeat;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl_topo_service.GwListGetRequest;
import com.lemonbeat.lsbl.lsbl_topo_service.TopoCmd;
import com.rabbitmq.client.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

public class Main {

    public static Properties settings;
    public static Connection connection;

    public static void main(String[] args) {
        loadSettings();
        setupConnection();

        for(int i = 0; i < 10; i++){
            Lsbl lsbl = LsBL.create(null, "SERVICE.TOPOSERVICE", ThreadLocalRandom.current().nextInt(1, 65535));
            Lsbl.Cmd cmd = new Lsbl.Cmd();
            TopoCmd topoCmd = new TopoCmd();
            GwListGetRequest gwListGetRequest = new GwListGetRequest();
            topoCmd.setGwListGet(gwListGetRequest);
            cmd.setTopoCmd(topoCmd);
            lsbl.setCmd(cmd);
            Lsbl response = callLsbl(lsbl);
            System.out.println(LsBL.write(response));
        }

    }

    public static Lsbl callLsbl(Lsbl request) {
        Lsbl result = null;
        try {
            Channel channel = connection.createChannel();
            String replyQueueName = channel.queueDeclare(randomReplyQueueName(), false, true, true, null).getQueue();
            channel.queueBind(replyQueueName, "DMZ", replyQueueName);
            request.getAdr().setSrc(replyQueueName);
            channel.basicPublish( "DMZ", request.getAdr().getTarget(), null, LsBL.write(request).getBytes( "UTF-8" ) );
            final BlockingQueue<Lsbl> response = new ArrayBlockingQueue<>(1);
            String consumerTag = channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body ){
                    try{
                        String lsblXML = new String( body, StandardCharsets.UTF_8.name());
                        Lsbl responseLsbl = LsBL.parse(lsblXML);
                        if ( responseLsbl.getAdr().getSeq() == request.getAdr().getSeq()) {
                            response.offer( responseLsbl );
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        response.offer(new Lsbl());
                    }
                }
            } );
            result = response.take();
            channel.basicCancel(consumerTag);
            channel.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }

    private static void setupConnection() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(settings.getProperty("BROKER_HOST", "localhost"));
            factory.setUsername(settings.getProperty("BROKER_USERNAME", "guest"));
            factory.setPassword(settings.getProperty("BROKER_PASSWORD", "guest"));
            factory.setPort(Integer.parseInt(settings.getProperty("BROKER_PORT", "5674")));
            factory.useSslProtocol("TLSv1.2");
            connection = factory.newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private static void loadCertificates() {
        // TODO: https://www.rabbitmq.com/ssl.html
    }

    private static String randomReplyQueueName() {
        int replyQueueNum = ThreadLocalRandom.current().nextInt(1000000, 9999999);
        return "SERVICE.JAVACLIENT."+System.currentTimeMillis()+replyQueueNum;
    }

    private static void loadSettings() {
        try {
            settings = new Properties();
            settings.load(new FileInputStream("settings.properties"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }



}
