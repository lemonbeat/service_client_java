package com.lemonbeat;

import com.lemonbeat.lsdl.LsDL;
import com.lemonbeat.lsdl.value.ValueReportType;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.util.List;

import static org.junit.Assert.*;

public class EventsExamplesTest {

    @Test
    public void subscribeToValueReports() {
        ServiceClient serviceClient = new ServiceClient("settings.properties");
        String consumerTag = serviceClient.subscribe("EVENT.APP.VALUESERVICE.DEVICE_VALUE_REPORTED", event -> {

            String deviceSgtin = event.getEvent().getValueEvent().getDeviceValueReported().getDeviceSgtin();
            String gwSgtin = event.getEvent().getValueEvent().getDeviceValueReported().getGwSgtin();
            String lsdlXML = event.getEvent().getValueEvent().getDeviceValueReported().getLsdl();

            try {
                // Get the values from the Lsdl value_report
                com.lemonbeat.lsdl.value.Network lsdl = (com.lemonbeat.lsdl.value.Network)LsDL.parse(lsdlXML, com.lemonbeat.lsdl.value.Network.class);
                List<Object> valueReports = lsdl.getDevice().get(0).getValueGetOrValueReportOrValueSet();
                for(Object i : valueReports){
                    ValueReportType valueReport = (ValueReportType) i;
                    Long valueId = valueReport.getValueId();
                    Double valueNumber = valueReport.getNumber();
                    // Do something with the values
                }
            } catch(JAXBException ex){
                ex.printStackTrace();
            }
        });
        assertNotEquals("ConsumerTag for subscription", null, consumerTag);
    }

    @Test
    public void subscribeToMetadataValueReports() {
        ServiceClient serviceClient = new ServiceClient("settings.properties");
        String consumerTag = serviceClient.subscribe("EVENT.APP.METADATASERVICE.METADATA_DEVICE_VALUE_REPORTED", event -> {

            String deviceUuid = event.getEvent().getMetadataEvent().getMetadataDeviceValueReported().getUuid();
            String deviceSgtin = event.getEvent().getMetadataEvent().getMetadataDeviceValueReported().getDeviceSgtin();
            String gwSgtin = event.getEvent().getMetadataEvent().getMetadataDeviceValueReported().getGwSgtin();
            String lsdlXML = event.getEvent().getMetadataEvent().getMetadataDeviceValueReported().getLsdl();

        });
        assertNotEquals("ConsumerTag for subscription", null, consumerTag);
    }

    @Test
    public void subscribeToMetadataDeviceIncluded() {
        ServiceClient serviceClient = new ServiceClient("settings.properties");
        String consumerTag = serviceClient.subscribe("EVENT.APP.METADATASERVICE.METADATA_DEVICE_INCLUDED", event -> {

            String deviceUuid = event.getEvent().getMetadataEvent().getMetadataDeviceIncluded().getUuid();
            String deviceSgtin = event.getEvent().getMetadataEvent().getMetadataDeviceIncluded().getDeviceSgtin();
            String gwSgtin = event.getEvent().getMetadataEvent().getMetadataDeviceIncluded().getGwSgtin();
            String lsdlXML = event.getEvent().getMetadataEvent().getMetadataDeviceIncluded().getLsdl();

        });
        assertNotEquals("ConsumerTag for subscription", null, consumerTag);
    }

}
