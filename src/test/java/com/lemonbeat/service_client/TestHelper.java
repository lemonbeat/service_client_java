package com.lemonbeat.service_client;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl.MessageType;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataAddedEvent;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataAttribute;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataEvent;
import com.rabbitmq.client.*;

import java.util.Random;
import java.util.UUID;

/**
 * Various methods to generate test data for unit tests.
 */
public class TestHelper {

    /**
     * Random 24 character SGTIN generator.
     * @return String with 24 hex characters
     */
    public static String randomSgtin() {
        Random random = new Random();
        StringBuffer result = new StringBuffer();
        String allowedChars = "0123456789ABCDEF";
        for(int i = 0; i < 24; i += 1){
            int index = random.nextInt(allowedChars.length());
            result.append(allowedChars.charAt(index));
        }
        return result.toString();
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
     * @param connection AMQP connection
     * @return Lsbl event that was published
     */
    public static Lsbl publishTestEvent(Connection connection, String eventName) throws Exception {
        Lsbl lsbl = LsBL.create("SERVICE.TEST", eventName, 42, MessageType.LSBL_EVENT);
        Lsbl.Event event = new Lsbl.Event();
        MetadataEvent metadataEvent = new MetadataEvent();
        MetadataAddedEvent metadataAddedEvent = new MetadataAddedEvent();
        metadataAddedEvent.setSgtin(randomSgtin());
        metadataAddedEvent.setUuid(randomUuid());
        metadataEvent.setMetadataAdded(metadataAddedEvent);
        event.setMetadataEvent(metadataEvent);
        lsbl.setEvent(event);

        Channel channel = connection.createChannel();
        channel.basicPublish("EVENT.APP", eventName, null, LsBL.write(lsbl).getBytes("UTF-8"));
        return lsbl;
    }

}
