package com.lemonbeat.service_client;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl.MessageType;
import com.lemonbeat.lsbl.lsbl_common.CommonResponse;
import com.lemonbeat.lsbl.lsbl_common_base_types.AckResponse;
import com.lemonbeat.lsbl.lsbl_common_base_types.NackResponse;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataAddedEvent;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataAttribute;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataEvent;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Various methods to generate test data for unit tests.
 */
public class TestHelper {

    /**
     * Returns a random string with hex characters with the given length.
     * @param length Length of the string
     * @return Random Hex String
     */
    public static String randomString(int length) {
        return randomString(length, "0123456789ABCDEF");
    }

    /**
     * Returns with the specified lenght and the given characters.
     * @param length Length of the string
     * @param allowedChars Allowed characters
     * @return String with the given length and the allowed characters.
     */
    public static String randomString(int length, String allowedChars) {
        Random random = new Random();
        StringBuffer result = new StringBuffer();
        for(int i = 0; i < length; i += 1){
            int index = random.nextInt(allowedChars.length());
            result.append(allowedChars.charAt(index));
        }
        return result.toString();
    }

    /**
     * Random 24 character SGTIN generator.
     * @return String with 24 hex characters
     */
    public static String randomSgtin() {
        return randomString(24);
    }

    /**
     * Random 12 character MAC generator.
     * @return String with 12 hex characters
     */
    public static String randomMac() {
        return randomString(12);
    }

    /**
     * Generate a random UUID
     * @return Random UUID as string
     */
    public static String randomUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Create a metadata attribute object.
     * @param name Key name
     * @param value Value
     * @return MetadataAttribute object with key and value
     */
    public static MetadataAttribute createMetadataAttribute(String name, String value) {
        MetadataAttribute metadataAttribute = new MetadataAttribute();
        metadataAttribute.setName(name);
        metadataAttribute.setValue(value);
        return metadataAttribute;
    }

    /**
     * Publishes a test event with the given name.
     * @param serviceClient ServiceClient instance with the current connection
     * @return Lsbl event that was published
     */
    public static Lsbl publishTestEvent(ServiceClient serviceClient, String eventName) throws Exception {
        Lsbl lsbl = LsBL.create("SERVICE.TEST", eventName, 42, MessageType.LSBL_EVENT);
        Lsbl.Event event = new Lsbl.Event();
        MetadataEvent metadataEvent = new MetadataEvent();
        MetadataAddedEvent metadataAddedEvent = new MetadataAddedEvent();
        metadataAddedEvent.setSgtin(randomSgtin());
        metadataAddedEvent.setUuid(randomUuid());
        metadataEvent.setMetadataAdded(metadataAddedEvent);
        event.setMetadataEvent(metadataEvent);
        lsbl.setEvent(event);

        Channel channel = serviceClient.getConnection().createChannel();
        channel.basicPublish("EVENT.APP", eventName, null, LsBL.write(lsbl).getBytes("UTF-8"));
        return lsbl;
    }

    /**
     * Mocks a service ACK response.
     * Returns the received command for further assertions.
     * @param serviceClient ServiceClient instance with the current connection
     * @param serviceQueue Service that will be mocked.
     * @return CompletableFuture with the command that was sent to the mocked service.
     */
    public static CompletableFuture<Lsbl> mockServiceResponseAck(ServiceClient serviceClient, String serviceQueue) throws Exception {
        Lsbl ack = LsBL.create(null, null, 42, MessageType.LSBL_APP_ACK);
        Lsbl.Response response = new Lsbl.Response();
        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setAck(new AckResponse());
        response.setCommonResponse(commonResponse);
        ack.setResponse(response);
        return mockServiceResponse(serviceClient, serviceQueue, ack);
    }

    /**
     * Mocks a service response and replies with the given Lsbl message.
     * Returns the received command for further assertions.
     * @param serviceClient ServiceClient instance with the current connection
     * @param serviceQueue Service that will be mocked.
     * @param response Lsbl response that should be returned by the mocked service.
     * @return CompletableFuture with the command that was sent to the mocked service.
     */
    public static CompletableFuture<Lsbl> mockServiceResponse(ServiceClient serviceClient, String serviceQueue, Lsbl response) throws Exception {
        CompletableFuture<Lsbl> callReceived = new CompletableFuture<>();

        String randomQueueName = "MOCK." + serviceQueue + "." + randomUuid();

        Channel channel = serviceClient.getConnection().createChannel();
        channel.queueDeclare(randomQueueName, false, true, true, null);
        channel.queueBind(randomQueueName, "DMZ", serviceQueue);
        channel.basicConsume(randomQueueName, false, new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                Lsbl received = null;
                try {
                    String lsblXML = new String(body, StandardCharsets.UTF_8.name());
                    received = LsBL.parse(lsblXML);

                    response.getAdr().setSeq(received.getAdr().getSeq());
                    response.getAdr().setSrc(received.getAdr().getTarget());
                    response.getAdr().setTarget(received.getAdr().getSrc());

                    channel.basicAck(envelope.getDeliveryTag(), true);
                    channel.basicPublish("PARTNER", received.getAdr().getSrc(), null, LsBL.write(response).getBytes("UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    callReceived.complete(received);
                }
            }
        });
        return callReceived;
    }

    /**
     * Creates a Nack response, this used to mock error responses.
     * @return Lsbl Nack Response Lsbl
     */
    public static Lsbl createNack() {
        return createNack("Error message", null, null);
    }

    /**
     * Creates a Nack response, this used to mock error responses.
     * @param message Message of the Nack
     * @return Lsbl Nack Response Lsbl
     */
    public static Lsbl createNack(String message) {
        return createNack(message, null, null);
    }

    /**
     * Creates a Nack response, this used to mock error responses.
     * @param message Message of the Nack
     * @param serviceCode Code of the service
     * @param errorCode Error code as string
     * @return Lsbl Nack Response Lsbl
     */
    public static Lsbl createNack(String message, String serviceCode, String errorCode) {
        Lsbl nack = LsBL.create(null, null, 42, MessageType.LSBL_APP_NACK);
        Lsbl.Response response = new Lsbl.Response();
        CommonResponse commonResponse = new CommonResponse();
        NackResponse nackResponse = new NackResponse();
        nackResponse.setMessage(message);
        nackResponse.setServiceCode(serviceCode);
        nackResponse.setErrorCode(errorCode);
        nackResponse.setTimestamp(BigInteger.valueOf(Calendar.getInstance().getTimeInMillis()));
        commonResponse.setNack(nackResponse);
        response.setCommonResponse(commonResponse);
        nack.setResponse(response);
        return nack;
    }

    /**
     * Creates a device description report with a randon SGTIN and MAC.
     * @return LsDL Device Description Report XML
     */
    public static String createDeviceDescriptionReport() {
        return createDeviceDescriptionReport(randomSgtin(), randomMac());
    }

    /**
     * Returns a LsDL device description report with the given SGTIN and MAC.
     * @param sgtin SGTIN
     * @param mac MAC
     * @return LsDL Device Description Report XML
     */
    public static String createDeviceDescriptionReport(String sgtin, String mac) {
        return "<network xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:device_descriptionxsd\" version=\"1\" xsi:noNamespaceSchemaLocation=\"../../xsd/device_description.xsd\">\n" +
            "<device version=\"1\">\n" +
                "<device_description_report>\n" +
                    "<!--  Type  -->\n" +
                    "<info number=\"1\" type_id=\"1\"/>\n" +
                    "<!--  Manufacturer  -->\n" +
                    "<info hex=\"" + sgtin + "\" type_id=\"2\"/>\n" +
                    "<!--  Sgtin  -->\n" +
                    "<info hex=\"" + mac + "\" type_id=\"3\"/>\n" +
                    "<!--  Mac Address  -->\n" +
                    "<info number=\"1\" type_id=\"4\"/>\n" +
                    "<!--  Hardware Version  -->\n" +
                    "<info number=\"14\" type_id=\"5\"/>\n" +
                    "<!--  Bootloader Version  -->\n" +
                    "<info number=\"1\" type_id=\"6\"/>\n" +
                    "<!--  Stack Version  -->\n" +
                    "<info hex=\"00340080\" type_id=\"7\"/>\n" +
                    "<!--  Application Version  -->\n" +
                    "<info number=\"500\" type_id=\"8\"/>\n" +
                    "<!--  Protocol  -->\n" +
                    "<info number=\"10000\" type_id=\"9\"/>\n" +
                    "<!--  Product  -->\n" +
                    "<info number=\"150\" type_id=\"10\"/>\n" +
                    "<!--  Included  -->\n" +
                    "<info number=\"2\" type_id=\"11\"/>\n" +
                    "<!--  Name  -->\n" +
                    "<info number=\"4\" type_id=\"12\"/>\n" +
                    "<!--  Radio Mode  -->\n" +
                    "<info number=\"1\" type_id=\"13\"/>\n" +
                    "<!--  Wakeup Interval  -->\n" +
                    "<info string=\"Device name\" type_id=\"14\"/>\n" +
                "</device_description_report>\n" +
            "</device>\n" +
        "</network>";
    }

    public static String createValueReport() {
        return "<network xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:valuexsd\" version=\"1\" xsi:noNamespaceSchemaLocation=\"../../xsd/value.xsd\">\n" +
                    "<device version=\"1\">\n" +
                        "<value_report value_id=\"1\" timestamp=\"0\" number=\"55.0\"/>\n" +
                    "</device>\n" +
                "</network>";
    }
    
    public static String createValueDescriptionReport() {
        return "<network xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:value_descriptionxsd\" version=\"1\" xsi:noNamespaceSchemaLocation=\"../../xsd/value_description.xsd\">\n" +
                    "<device version=\"1\">\n" +
                        "<value_description_report>\n" +
                            "<value_description value_id=\"1\" type_id=\"14\" mode=\"2\" persistent=\"0\" name=\"light\" min_log_interval=\"300\" max_log_values=\"20\">\n" +
                                "<number_format unit=\"W\" min=\"0.0\" max=\"1.0\" step=\"1.0\"/>\n" +
                            "</value_description>\n" +
                        "</value_description_report>\n" +
                    "</device>\n" +
                "</network>\n";
    }

}
