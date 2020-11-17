package com.lemonbeat.service_client;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl.MessageType;
import com.lemonbeat.lsbl.lsbl_topo_service.TopoResponse;
import com.lemonbeat.lsbl.lsbl_value_service.ValueDescriptionGetResponse;
import com.lemonbeat.lsbl.lsbl_value_service.ValueGetResponse;
import com.lemonbeat.lsbl.lsbl_value_service.ValueResponse;
import com.lemonbeat.lsdl.LsDL;
import com.lemonbeat.lsdl.value.ValueSetType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

public class ValueServiceClientTest {

    static ServiceClient serviceClient;
    static ValueServiceClient valueServiceClient;

    @Before
    public void setupUserServiceClient() throws InterruptedException {
        serviceClient = new ServiceClient("settings.properties");
        valueServiceClient = new ValueServiceClient(serviceClient);
    }

    @After
    public void cleanup() {
        try {
            serviceClient.getConnection().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getValuesBySgtin() throws Exception {
        String deviceSgtin = TestHelper.randomSgtin();
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        Lsbl serviceResponse = createValueGetResponse(deviceSgtin);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.VALUESERVICE", serviceResponse);

        valueServiceClient.getValuesBySgtin(deviceSgtin, lsbl -> {
            result.complete(lsbl);
        });

        Lsbl sent = mock.get();
        assert sent.getCmd().getValueCmd().getValueGet().getDeviceSgtin().equals(deviceSgtin);

        Lsbl response = result.get();
        assert LsBL.isResponse(response);
    }

    @Test
    public void getValuesBySgtinAwait() throws Exception {
        String deviceSgtin = TestHelper.randomSgtin();

        Lsbl serviceResponse = createValueGetResponse(deviceSgtin);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.VALUESERVICE", serviceResponse);

        Lsbl response = valueServiceClient.getValuesBySgtinAwait(deviceSgtin);

        Lsbl sent = mock.get();
        assert sent.getCmd().getValueCmd().getValueGet().getDeviceSgtin().equals(deviceSgtin);

        assert LsBL.isResponse(response);
    }

    @Test
    public void getValuesByUuid() throws Exception {
        String deviceUuid = TestHelper.randomUuid();
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        Lsbl serviceResponse = createValueGetResponse(deviceUuid);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.VALUESERVICE", serviceResponse);

        valueServiceClient.getValuesByUuid(deviceUuid, lsbl -> {
            result.complete(lsbl);
        });

        Lsbl sent = mock.get();
        assert sent.getCmd().getValueCmd().getValueGet().getDeviceUuid().equals(deviceUuid);

        Lsbl response = result.get();
        assert LsBL.isResponse(response);
    }

    @Test
    public void getValuesByUuidAwait() throws Exception {
        String deviceUuid = TestHelper.randomUuid();

        Lsbl serviceResponse = createValueGetResponse(deviceUuid);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.VALUESERVICE", serviceResponse);

        Lsbl response = valueServiceClient.getValuesByUuidAwait(deviceUuid);

        Lsbl sent = mock.get();
        assert sent.getCmd().getValueCmd().getValueGet().getDeviceUuid().equals(deviceUuid);
        assert LsBL.isResponse(response);
    }

    @Test
    public void getValueDescriptionBySgtin() throws Exception {
        String deviceSgtin = TestHelper.randomSgtin();
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        Lsbl serviceResponse = createValueDescriptionGetResponse(deviceSgtin);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.VALUESERVICE", serviceResponse);

        valueServiceClient.getValueDescriptionBySgtin(deviceSgtin, lsbl -> {
            result.complete(lsbl);
        });

        Lsbl sent = mock.get();
        assert sent.getCmd().getValueCmd().getValueDescriptionGet().getDeviceSgtin().equals(deviceSgtin);

        Lsbl response = result.get();
        assert LsBL.isResponse(response);
    }

    @Test
    public void getValueDescriptionBySgtinAwait() throws Exception {
        String deviceSgtin = TestHelper.randomSgtin();

        Lsbl serviceResponse = createValueDescriptionGetResponse(deviceSgtin);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.VALUESERVICE", serviceResponse);

        Lsbl response = valueServiceClient.getValueDescriptionBySgtinAwait(deviceSgtin);

        Lsbl sent = mock.get();
        assert sent.getCmd().getValueCmd().getValueDescriptionGet().getDeviceSgtin().equals(deviceSgtin);
        assert LsBL.isResponse(response);
    }

    @Test
    public void getValueDescriptionByUuid() throws Exception {
        String deviceUuid = TestHelper.randomUuid();
        String deviceSgtin = TestHelper.randomSgtin();
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        Lsbl serviceResponse = createValueDescriptionGetResponse(deviceSgtin);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.VALUESERVICE", serviceResponse);

        valueServiceClient.getValueDescriptionByUuid(deviceUuid, lsbl -> {
            result.complete(lsbl);
        });

        Lsbl sent = mock.get();
        assert sent.getCmd().getValueCmd().getValueDescriptionGet().getDeviceUuid().equals(deviceUuid);

        Lsbl response = result.get();
        assert LsBL.isResponse(response);
    }

    @Test
    public void getValueDescriptionByUuidAwait() throws Exception {
        String deviceUuid = TestHelper.randomUuid();
        String deviceSgtin = TestHelper.randomSgtin();

        Lsbl serviceResponse = createValueDescriptionGetResponse(deviceSgtin);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.VALUESERVICE", serviceResponse);

        Lsbl response = valueServiceClient.getValueDescriptionByUuidAwait(deviceUuid);

        Lsbl sent = mock.get();
        assert sent.getCmd().getValueCmd().getValueDescriptionGet().getDeviceUuid().equals(deviceUuid);
        assert LsBL.isResponse(response);
    }

    @Test
    public void setValueBySgtin () throws Exception {
        String deviceSgtin = TestHelper.randomSgtin();
        List<ValueSetType> values = createValueSetList();
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponseAck(serviceClient, "SERVICE.VALUESERVICE");

        valueServiceClient.setValueBySgtin(deviceSgtin, values, lsbl -> {
            result.complete(lsbl);
        });

        Lsbl sent = mock.get();

        assert sent.getCmd().getValueCmd().getValueSet().getDeviceSgtin().equals(deviceSgtin);

        String sentLsdl = sent.getCmd().getValueCmd().getValueSet().getLsdl();
        assert isExpectedLsdl(values, sentLsdl);

        Lsbl response = result.get();
        assert LsBL.isAck(response);
    }

    @Test
    public void setValueBySgtinWithRetries() throws Exception {
        String deviceSgtin = TestHelper.randomSgtin();
        List<ValueSetType> values = createValueSetList();
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponseAck(serviceClient, "SERVICE.VALUESERVICE");

        valueServiceClient.setValueBySgtin(deviceSgtin, values, 5, lsbl -> {
            result.complete(lsbl);
        });

        Lsbl sent = mock.get();
        assert sent.getCmd().getValueCmd().getValueSet().getDeviceSgtin().equals(deviceSgtin);
        String sentLsdl = sent.getCmd().getValueCmd().getValueSet().getLsdl();
        assert isExpectedLsdl(values, sentLsdl);
        assert sent.getCmd().getValueCmd().getValueSet().getRetries() == 5;

        Lsbl response = result.get();
        assert LsBL.isAck(response);
    }

    @Test
    public void setValueBySgtinAwait () throws Exception {
        String deviceSgtin = TestHelper.randomSgtin();
        List<ValueSetType> values = createValueSetList();
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponseAck(serviceClient, "SERVICE.VALUESERVICE");

        Lsbl response = valueServiceClient.setValueBySgtinAwait(deviceSgtin, values);

        Lsbl sent = mock.get();
        assert sent.getCmd().getValueCmd().getValueSet().getDeviceSgtin().equals(deviceSgtin);
        assert LsBL.isAck(response);
    }

    @Test
    public void setValueBySgtinAwaitWithRetries () throws Exception {
        String deviceSgtin = TestHelper.randomSgtin();
        List<ValueSetType> values = createValueSetList();
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponseAck(serviceClient, "SERVICE.VALUESERVICE");

        Lsbl response = valueServiceClient.setValueBySgtinAwait(deviceSgtin, values, 5);

        Lsbl sent = mock.get();
        assert sent.getCmd().getValueCmd().getValueSet().getDeviceSgtin().equals(deviceSgtin);
        assert sent.getCmd().getValueCmd().getValueSet().getRetries() == 5;
        assert LsBL.isAck(response);
    }

    @Test
    public void setValueByUuid () throws Exception {
        String deviceUuid = TestHelper.randomUuid();
        List<ValueSetType> values = createValueSetList();
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponseAck(serviceClient, "SERVICE.VALUESERVICE");

        valueServiceClient.setValueByUuid(deviceUuid, values, lsbl -> {
            result.complete(lsbl);
        });

        Lsbl sent = mock.get();

        assert sent.getCmd().getValueCmd().getValueSet().getDeviceUuid().equals(deviceUuid);

        String sentLsdl = sent.getCmd().getValueCmd().getValueSet().getLsdl();
        assert isExpectedLsdl(values, sentLsdl);

        Lsbl response = result.get();
        assert LsBL.isAck(response);
    }

    @Test
    public void setValueByUuidWithRetries () throws Exception {
        String deviceUuid = TestHelper.randomUuid();
        List<ValueSetType> values = createValueSetList();
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponseAck(serviceClient, "SERVICE.VALUESERVICE");

        valueServiceClient.setValueByUuid(deviceUuid, values, 5, lsbl -> {
            result.complete(lsbl);
        });

        Lsbl sent = mock.get();
        assert sent.getCmd().getValueCmd().getValueSet().getDeviceUuid().equals(deviceUuid);
        String sentLsdl = sent.getCmd().getValueCmd().getValueSet().getLsdl();
        assert isExpectedLsdl(values, sentLsdl);
        assert sent.getCmd().getValueCmd().getValueSet().getRetries() == 5;

        Lsbl response = result.get();
        assert LsBL.isAck(response);
    }

    @Test
    public void setValueByUuidAwait () throws Exception {
        String deviceUuid = TestHelper.randomUuid();
        List<ValueSetType> values = createValueSetList();
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponseAck(serviceClient, "SERVICE.VALUESERVICE");

        Lsbl response = valueServiceClient.setValueByUuidAwait(deviceUuid, values);

        Lsbl sent = mock.get();
        assert sent.getCmd().getValueCmd().getValueSet().getDeviceUuid().equals(deviceUuid);
        assert LsBL.isAck(response);
    }

    @Test
    public void setValueByUuidAwaitWithRetries () throws Exception {
        String deviceUuid = TestHelper.randomUuid();
        List<ValueSetType> values = createValueSetList();
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponseAck(serviceClient, "SERVICE.VALUESERVICE");

        Lsbl response = valueServiceClient.setValueByUuidAwait(deviceUuid, values, 5);

        Lsbl sent = mock.get();
        assert sent.getCmd().getValueCmd().getValueSet().getDeviceUuid().equals(deviceUuid);
        assert sent.getCmd().getValueCmd().getValueSet().getRetries() == 5;
        assert LsBL.isAck(response);
    }


    public static List<ValueSetType> createValueSetList() {
        ArrayList<ValueSetType> list = new ArrayList<>();
        for(int i = 0; i < 16; i++){
            ValueSetType set = new ValueSetType();
            set.setValueId(i);
            set.setNumber(42.0);
            list.add(set);
        }
        return list;
    }

    public static boolean isExpectedLsdl(List<ValueSetType> expected, String sent) throws JAXBException {
        Object obj = LsDL.parse(sent, com.lemonbeat.lsdl.value.Network.class);
        com.lemonbeat.lsdl.value.Network lsdl = (com.lemonbeat.lsdl.value.Network) obj;
        return lsdl.getDevice().get(0).getValueGetOrValueReportOrValueSet().size() == expected.size();
    }

    public static Lsbl createValueDescriptionGetResponse(String deviceSgtin) {
        ValueResponse valueResponse = new ValueResponse();
        ValueDescriptionGetResponse valueDescriptionGetResponse = new ValueDescriptionGetResponse();
        valueDescriptionGetResponse.setDeviceSgtin(deviceSgtin);
        valueDescriptionGetResponse.setDeviceMac(TestHelper.randomMac());
        valueDescriptionGetResponse.setGwSgtin(TestHelper.randomSgtin());
        valueDescriptionGetResponse.setRadioMode(0);
        valueDescriptionGetResponse.setLsdl(TestHelper.createValueDescriptionReport());
        valueResponse.setValueDescriptionGet(valueDescriptionGetResponse);
        return createValueResponse(valueResponse);
    }


    public static Lsbl createValueGetResponse(String deviceSgtin) {
        ValueResponse valueResponse = new ValueResponse();
        ValueGetResponse valueGetResponse = new ValueGetResponse();
        valueGetResponse.setDeviceSgtin(deviceSgtin);
        valueGetResponse.setDeviceMac(TestHelper.randomMac());
        valueGetResponse.setGwSgtin(TestHelper.randomSgtin());
        valueGetResponse.setRadioMode(0);
        valueGetResponse.setLsdl(TestHelper.createValueReport());
        valueResponse.setValueGet(valueGetResponse);
        return createValueResponse(valueResponse);
    }

    private static Lsbl createValueResponse(ValueResponse valueResponse) {
        Lsbl lsbl = LsBL.create(null, null, 42, MessageType.LSBL_RESPONSE);
        Lsbl.Response response = new Lsbl.Response();
        response.setValueResponse(valueResponse);
        lsbl.setResponse(response);
        return lsbl;
    }



}
