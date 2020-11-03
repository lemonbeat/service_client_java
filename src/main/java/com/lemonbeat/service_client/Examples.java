package com.lemonbeat.service_client;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl_common_base_types.NackResponse;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataAttribute;
import com.lemonbeat.lsbl.lsbl_value_service.ValueDescriptionGetResponse;
import com.lemonbeat.lsbl.lsbl_value_service.ValueGetResponse;
import com.lemonbeat.lsdl.LsDL;
import com.lemonbeat.lsdl.device_description.InfoType;
import com.lemonbeat.lsdl.value.ValueReportType;
import com.lemonbeat.lsdl.value.ValueSetType;
import com.lemonbeat.lsdl.value_description.ValueDescriptionType;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

/**
 * Various examples for common commands.
 */
public class Examples {


    public static void example_gw_list_get(ServiceClient serviceClient) {
        TopoServiceClient topoServiceClient = new TopoServiceClient(serviceClient);

        Lsbl gwListResponse = topoServiceClient.getGatewayListAwait();
        List<String> gwList = gwListResponse.getResponse().getTopoResponse().getGwListGet().getGw();
        for(String gw: gwList){
            System.out.println(gw);
        }
    }

    public static void example_gw_device_list_get(ServiceClient serviceClient, String gatewaySgtin) {
        TopoServiceClient topoServiceClient = new TopoServiceClient(serviceClient);
        Lsbl gwDeviceList = topoServiceClient.getDeviceListAwait(gatewaySgtin);

        List<String> includedDevices = gwDeviceList.getResponse().getTopoResponse().getGwDeviceListGet().getDeviceList().getLsdl();
        List<String> unincludedDevices = gwDeviceList.getResponse().getTopoResponse().getGwDeviceListGet().getDeviceList().getLsdl();

        System.out.println("Included devices:");
        for(String i: includedDevices) {
            try {
                List<InfoType> deviceDescription = LsDL.parseDeviceDescriptionReport(i);
                System.out.println(i);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Unincluded devices:");
        for(String i: unincludedDevices) {
            System.out.println(i);
        }
    }

    public static void example_device_description_get(ServiceClient serviceClient, String deviceSgtin) {
        TopoServiceClient topoServiceClient = new TopoServiceClient(serviceClient);
        Lsbl deviceDescriptionResponse = topoServiceClient.getDeviceDescriptionAwait(deviceSgtin);

        String lsdl = deviceDescriptionResponse.getResponse().getTopoResponse().getDeviceDescriptionGet().getLsdl();
        try {
            List<InfoType> deviceDescriptionItems = LsDL.parseDeviceDescriptionReport(lsdl);
            for(InfoType i : deviceDescriptionItems) {
                System.out.println(
                        "Type ID: " + i.getTypeId() + ", " +
                        "String: " + i.getString() + ", " +
                        "Number: " + i.getNumber() + ", " +
                        "HexBinary: " + i.getHex()
                );
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static void example_metadata_get(ServiceClient serviceClient, String deviceSgtin) {
        MetadataServiceClient metadataServiceClient = new MetadataServiceClient(serviceClient);
        metadataServiceClient.getMetadataBySgtin(deviceSgtin, metadataBySgtin -> {
            System.out.println(LsBL.write(metadataBySgtin));
        });
    }

    public static void example_metadata_get_by_uuid(ServiceClient serviceClient, String deviceUUID) {
        MetadataServiceClient metadataServiceClient = new MetadataServiceClient(serviceClient);

        metadataServiceClient.getMetadataByUuid(deviceUUID, metadataByUuid -> {
            if(LsBL.isResponse(metadataByUuid)) {
                List<MetadataAttribute> metadata = metadataByUuid.getResponse().getMetadataResponse().getMetadataReport().getAttribute();
                for(MetadataAttribute i: metadata){
                    System.out.println(i.getName() + " => " + i.getValue());
                }

            } else {
                NackResponse nack = metadataByUuid.getResponse().getCommonResponse().getNack();
                System.out.println("MetadataGet failed, reason: " + nack.getMessage());
            }

        });
    }

    public static void example_value_get(ServiceClient serviceClient, String deviceSgtin) {
        ValueServiceClient valueServiceClient = new ValueServiceClient(serviceClient);

        valueServiceClient.getValuesBySgtin(deviceSgtin, response -> {
            if(LsBL.isResponse(response)){

                // The value report is in the LsDL element of the response.
                try {
                    ValueGetResponse valueReport = response.getResponse().getValueResponse().getValueGet();
                    String lsdl = valueReport.getLsdl();
                    List<ValueReportType> valueReportItems = LsDL.parseValueReport(lsdl);

                    for(ValueReportType i: valueReportItems){
                        System.out.println(
                                "Value ID: " + i.getValueId() + ", " +
                                        "String: " + i.getString() + ", " +
                                        "Number: " + i.getNumber() + ", " +
                                        "HexBinary: " + i.getHexBinary() + ", " +
                                        "Timestamp: " + i.getTimestamp()
                        );
                    }

                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            } else {
                NackResponse nack = response.getResponse().getCommonResponse().getNack();
                System.out.println("ValueGet failed, reason: " + nack.getMessage());
            }
        });
    }

    public static void example_value_get_by_uuid(ServiceClient serviceClient, String deviceUuid) {
        ValueServiceClient valueServiceClient = new ValueServiceClient(serviceClient);

        valueServiceClient.getValuesByUuid(deviceUuid, valueGetResponse -> {
            System.out.println(LsBL.write(valueGetResponse));
        });
    }

    public static void example_value_description_get(ServiceClient serviceClient, String deviceSgtin) {
        ValueServiceClient valueServiceClient = new ValueServiceClient(serviceClient);
        valueServiceClient.getValueDescriptionBySgtin(deviceSgtin, response -> {
            if(LsBL.isResponse(response)){

                // The value_description_report is in the LsDL element of the response.
                try {
                    ValueDescriptionGetResponse valueDescription = response.getResponse().getValueResponse().getValueDescriptionGet();
                    String lsdl = valueDescription.getLsdl();
                    List<ValueDescriptionType> valueDescriptionItems = LsDL.parseValueDescriptionReport(lsdl);
                    for(ValueDescriptionType i: valueDescriptionItems) {

                        String formatInfo = "";

                        if(i.getHexBinaryFormat() != null){
                            formatInfo += "Type: hexBinary, ";
                            formatInfo += "MaxLength: " + i.getHexBinaryFormat().getMaxLength();
                        }
                        if(i.getNumberFormat() != null){
                            formatInfo += "Type: number, ";
                            formatInfo += "Unit: " + i.getNumberFormat().getUnit() + ", ";
                            formatInfo += "Max: " + i.getNumberFormat().getMax() + ", ";
                            formatInfo += "Min: " + i.getNumberFormat().getMin() + ", ";
                            formatInfo += "Step: " + i.getNumberFormat().getStep();
                        }
                        if(i.getStringFormat() != null){
                            formatInfo += "Type: string, ";
                            formatInfo += "MaxLength: " + i.getStringFormat().getMaxLength();
                        }

                        System.out.println(
                                "Value ID: " + i.getValueId() + ", " +
                                        "Name: " + i.getName() + ", " +
                                        "Type ID: " + i.getTypeId() + ", " +
                                        "Mode: " + i.getMode() + ", " +
                                        "Persistent " + i.getPersistent() + ", " +
                                        "Virtual: " + i.getVirtual() + ", " +
                                        formatInfo
                        );
                    }
                } catch (JAXBException e) {
                    e.printStackTrace();
                }

            } else {
                NackResponse nack = response.getResponse().getCommonResponse().getNack();
                System.out.println("ValueDescriptionGet failed, reason: " + nack.getMessage());
            }
        });
    }

    public static void example_value_set(ServiceClient serviceClient, String deviceSgtin) {
        ValueServiceClient valueServiceClient = new ValueServiceClient(serviceClient);

        ArrayList<ValueSetType> valueList = new ArrayList<>();

        ValueSetType value_1 = new ValueSetType();
        value_1.setValueId(1L);
        value_1.setNumber(1.0);
        valueList.add(value_1);

        valueServiceClient.setValueBySgtin(deviceSgtin, valueList, 1, valueSetResponse -> {
            if(LsBL.isAck(valueSetResponse)){
                System.out.println("Value set successful");
            }
            if(LsBL.isNack(valueSetResponse)){
                System.out.println("Value set failed: " + valueSetResponse.getResponse().getCommonResponse().getNack().getMessage());
            }
        });
    }







}
