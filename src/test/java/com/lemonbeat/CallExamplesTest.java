package com.lemonbeat;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl_config_mgmt_service.ConfigMgmtCmd;
import com.lemonbeat.lsbl.lsbl_config_mgmt_service.DeviceConfigurationFetchRequest;
import com.lemonbeat.lsbl.lsbl_config_mgmt_service.ServiceIdList;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataAttribute;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataCmd;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataGetRequest;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataSetRequest;
import com.lemonbeat.lsbl.lsbl_topo_service.DeviceDescriptionGetRequest;
import com.lemonbeat.lsbl.lsbl_topo_service.GwDescriptionGetRequest;
import com.lemonbeat.lsbl.lsbl_topo_service.GwListGetRequest;
import com.lemonbeat.lsbl.lsbl_topo_service.TopoCmd;
import com.lemonbeat.lsbl.lsbl_value_service.ValueCmd;
import com.lemonbeat.lsbl.lsbl_value_service.ValueGetRequest;
import com.lemonbeat.lsbl.lsbl_value_service.ValueSetRequest;
import com.lemonbeat.lsdl.LsDL;
import com.lemonbeat.lsdl.value.ValueSetType;
import org.junit.Test;

import javax.xml.bind.JAXBException;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class CallExamplesTest {

    @Test
    public void creatingLsblMessages() throws JAXBException {
        // The src and the seq will be set by the service client when sending the message.
        Lsbl lsbl = LsBL.create(null, "SERVICE.TOPOSERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();

        // Create a gw_description_get command
        TopoCmd topoCmd = new TopoCmd();
        GwDescriptionGetRequest gwDescriptionGetRequest = new GwDescriptionGetRequest();
        gwDescriptionGetRequest.setGwSgtin("SGTIN_OF_THE_GW");
        topoCmd.setGwDescriptionGet(gwDescriptionGetRequest);
        cmd.setTopoCmd(topoCmd);
        lsbl.setCmd(cmd);

        // Write it to a String
        String lsblXML = LsBL.write(lsbl);

        // Parse a String to Lsbl
        Lsbl lsblObj = LsBL.parse(lsblXML);
        assertEquals("SGTIN_OF_THE_GW", lsblObj.getCmd().getTopoCmd().getGwDescriptionGet().getGwSgtin());
    }

    @Test
    public void creatingLsdlMessages() throws JAXBException {
        // Every lemonbeat service has a seperate package, make sure you use the correct Network class.
        com.lemonbeat.lsdl.value.Network network = new com.lemonbeat.lsdl.value.Network();
        com.lemonbeat.lsdl.value.Network.Device device = new com.lemonbeat.lsdl.value.Network.Device();
        network.getDevice().add(device);

        // Create a value_set lsdl message
        for(int i = 0; i < 5; i++){
            ValueSetType valueSet = new ValueSetType();
            valueSet.setValueId(i);
            valueSet.setNumber(i*10.0);
            device.getValueGetOrValueReportOrValueSet().add(valueSet);
        }

        // Write it to a String
        String lsdlXML = LsDL.write(network);

        // Parse a String to Lsdl, you need to specify the correct Network class
        com.lemonbeat.lsdl.value.Network lsdlObj = (com.lemonbeat.lsdl.value.Network) LsDL.parse(lsdlXML, com.lemonbeat.lsdl.value.Network.class);
        assertTrue(lsdlObj.getDevice().size() == 1);
    }

    @Test
    public void getGatewayList() throws Exception {
        ServiceClient serviceClient = new ServiceClient("settings.properties");
        Properties settings = new Properties();
        settings.load(new FileInputStream("settings.properties"));
        UserServiceClient userServiceClient = new UserServiceClient(serviceClient);

        Lsbl lsbl = LsBL.create(null, "SERVICE.TOPOSERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        TopoCmd topoCmd = new TopoCmd();
        GwListGetRequest gwListGetRequest = new GwListGetRequest();
        topoCmd.setGwListGet(gwListGetRequest);
        cmd.setTopoCmd(topoCmd);
        lsbl.setCmd(cmd);

        userServiceClient.login(settings.getProperty("BACKEND_USERNAME"), settings.getProperty("BACKEND_PASSWORD"), loginResponse -> {
            serviceClient.call(lsbl, response -> {
                List<String> gateways = response.getResponse().getTopoResponse().getGwListGet().getGw();
                for(String gw: gateways){
                    // Do someting with the gateway sgtin
                }
                assertTrue(gateways.size() >= 0);
            });
        });

    }

    @Test
    public void getDeviceDescription() throws JAXBException {
        Lsbl lsbl = LsBL.create(null, "SERVICE.TOPOSERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        TopoCmd topoCmd = new TopoCmd();
        DeviceDescriptionGetRequest deviceDescriptionGetRequest = new DeviceDescriptionGetRequest();
        deviceDescriptionGetRequest.setDeviceSgtin("SGTIN_OF_THE_DEVICE");
        topoCmd.setDeviceDescriptionGet(deviceDescriptionGetRequest);
        cmd.setTopoCmd(topoCmd);
        lsbl.setCmd(cmd);

        // Write it to a String
        String lsblXML = LsBL.write(lsbl);

        // Parse a String to Lsbl
        Lsbl lsblObj = LsBL.parse(lsblXML);
        assertEquals("SGTIN_OF_THE_DEVICE", lsblObj.getCmd().getTopoCmd().getDeviceDescriptionGet().getDeviceSgtin());
    }


    @Test
    public void getValueDescriptionReport() throws JAXBException {
        Lsbl lsbl = LsBL.create(null, "SERVICE.CONFIGMGMTSERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        ConfigMgmtCmd configMgmtCmd = new ConfigMgmtCmd();
        DeviceConfigurationFetchRequest request = new DeviceConfigurationFetchRequest();
        request.setDeviceSgtin("SGTIN_OF_THE_DEVICE");
        request.setGwSgtin("SGTIN_OF_THE_GATEWAY");
        request.setDeviceMac("MAC_OF_THE_DEVICE");
        ServiceIdList list = new ServiceIdList();
        list.getServiceId().add(4L);
        request.setServiceIds(list);
        configMgmtCmd.setDeviceConfigurationFetch(request);
        cmd.setConfigMgmtCmd(configMgmtCmd);
        lsbl.setCmd(cmd);

        // Write it to a String
        String lsblXML = LsBL.write(lsbl);

        // Parse a String to Lsbl
        Lsbl lsblObj = LsBL.parse(lsblXML);
        assertEquals("SGTIN_OF_THE_DEVICE", lsblObj.getCmd().getConfigMgmtCmd().getDeviceConfigurationFetch().getDeviceSgtin());
    }


    @Test
    public void setMetadata() throws JAXBException {
        Lsbl lsbl = LsBL.create(null, "SERVICE.METADATASERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        MetadataCmd metadataCmd = new MetadataCmd();
        MetadataSetRequest metadataSetRequest = new MetadataSetRequest();
        metadataSetRequest.setSgtin("SGTIN_OF_THE_DEVICE");
        MetadataAttribute attribute = new MetadataAttribute();
        attribute.setName("Type");
        attribute.setValue("DDK");
        metadataSetRequest.getAttribute().add(attribute);
        metadataCmd.setMetadataSet(metadataSetRequest);
        cmd.setMetadataCmd(metadataCmd);
        lsbl.setCmd(cmd);

        // Write it to a String
        String lsblXML = LsBL.write(lsbl);

        // Parse a String to Lsbl
        Lsbl lsblObj = LsBL.parse(lsblXML);
        assertEquals("SGTIN_OF_THE_DEVICE", lsblObj.getCmd().getMetadataCmd().getMetadataSet().getSgtin());
    }

    @Test
    public void getMetadata() throws JAXBException {
        Lsbl lsbl = LsBL.create(null, "SERVICE.METADATASERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        MetadataCmd metadataCmd = new MetadataCmd();
        MetadataGetRequest metadataGetRequest = new MetadataGetRequest();
        metadataGetRequest.setSgtin("SGTIN_OF_THE_DEVICE");
        metadataCmd.setMetadataGet(metadataGetRequest);
        cmd.setMetadataCmd(metadataCmd);
        lsbl.setCmd(cmd);

        // Write it to a String
        String lsblXML = LsBL.write(lsbl);

        // Parse a String to Lsbl
        Lsbl lsblObj = LsBL.parse(lsblXML);
        assertEquals("SGTIN_OF_THE_DEVICE", lsblObj.getCmd().getMetadataCmd().getMetadataGet().getSgtin());
    }

    @Test
    public void setValues() throws JAXBException {
        Lsbl lsbl = LsBL.create(null, "SERVICE.VALUESERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        ValueCmd valueCmd = new ValueCmd();
        ValueSetRequest valueSetRequest = new ValueSetRequest();
        valueSetRequest.setDeviceSgtin("SGTIN_OF_THE_DEVICE");

        // Create a value_set lsdl message
        com.lemonbeat.lsdl.value.Network network = new com.lemonbeat.lsdl.value.Network();
        com.lemonbeat.lsdl.value.Network.Device device = new com.lemonbeat.lsdl.value.Network.Device();
        network.getDevice().add(device);
        for(int i = 0; i < 5; i++){
            ValueSetType valueSet = new ValueSetType();
            valueSet.setValueId(i);
            valueSet.setNumber(i * 1.5);
            device.getValueGetOrValueReportOrValueSet().add(valueSet);
        }
        valueSetRequest.setLsdl(LsDL.write(network));

        valueCmd.setValueSet(valueSetRequest);
        cmd.setValueCmd(valueCmd);
        lsbl.setCmd(cmd);

        // Write it to a String
        String lsblXML = LsBL.write(lsbl);

        // Parse a String to Lsbl
        Lsbl lsblObj = LsBL.parse(lsblXML);
        assertEquals("SGTIN_OF_THE_DEVICE", lsblObj.getCmd().getValueCmd().getValueSet().getDeviceSgtin());
    }

    @Test
    public void getValues() throws JAXBException {
        Lsbl lsbl = LsBL.create(null, "SERVICE.VALUESERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        ValueCmd valueCmd = new ValueCmd();
        ValueGetRequest valueGetRequest = new ValueGetRequest();
        valueGetRequest.setDeviceSgtin("SGTIN_OF_THE_DEVICE");
        valueCmd.setValueGet(valueGetRequest);
        cmd.setValueCmd(valueCmd);
        lsbl.setCmd(cmd);

        // Write it to a String
        String lsblXML = LsBL.write(lsbl);

        // Parse a String to Lsbl
        Lsbl lsblObj = LsBL.parse(lsblXML);
        assertEquals("SGTIN_OF_THE_DEVICE", lsblObj.getCmd().getValueCmd().getValueGet().getDeviceSgtin());
    }

}
