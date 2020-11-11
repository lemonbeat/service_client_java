package com.lemonbeat.service_client;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl.MessageType;
import com.lemonbeat.lsbl.lsbl_topo_service.*;
import com.lemonbeat.lsbl.lsbl_user_service.UserLoginResponse;
import com.lemonbeat.lsbl.lsbl_user_service.UserResponse;
import com.lemonbeat.lsdl.LsDL;
import com.lemonbeat.lsdl.device_description.InfoType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

public class TopoServiceClientTest {

    static ServiceClient serviceClient;
    static TopoServiceClient topoServiceClient;

    @Before
    public void setupTopoServiceClient() throws InterruptedException {
        serviceClient = new ServiceClient("settings.properties");
        topoServiceClient = new TopoServiceClient(serviceClient);
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
    public void getGatewayList() throws Exception {
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        Lsbl serviceResponse = createGatewayListResponse(5);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.TOPOSERVICE", serviceResponse);

        topoServiceClient.getGatewayList(lsbl -> {
            result.complete(lsbl);
        });

        Lsbl sent = mock.get();
        assert sent.getCmd().getTopoCmd().getGwListGet() != null;

        Lsbl response = result.get();
        assert LsBL.isResponse(response);
        assert response.getResponse().getTopoResponse().getGwListGet().getGw().size() == 5;
    }

    @Test
    public void getGatewayListAwait() throws Exception {
        Lsbl serviceResponse = createGatewayListResponse(5);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.TOPOSERVICE", serviceResponse);

        Lsbl response =  topoServiceClient.getGatewayListAwait();

        Lsbl sent = mock.get();
        assert sent.getCmd().getTopoCmd().getGwListGet() != null;

        assert LsBL.isResponse(response);
        assert response.getResponse().getTopoResponse().getGwListGet().getGw().size() == 5;
    }

    @Test
    public void getDeviceList() throws Exception {
        String gatewaySgtin = TestHelper.randomSgtin();
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        ArrayList<String> included = new ArrayList<>();
        ArrayList<String> unincluded = new ArrayList<>();

        Lsbl serviceResponse = createDeviceListResponse(included, unincluded);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.TOPOSERVICE", serviceResponse);

        topoServiceClient.getDeviceList(gatewaySgtin, lsbl -> {
            result.complete(lsbl);
        });

        Lsbl sent = mock.get();
        assert sent.getCmd().getTopoCmd().getGwDeviceListGet().getGwSgtin().equals(gatewaySgtin);

        Lsbl response = result.get();
        assert LsBL.isResponse(response);
    }

    @Test
    public void getDeviceListAwait() throws Exception {
        String gatewaySgtin = TestHelper.randomSgtin();
        ArrayList<String> included = new ArrayList<>();
        ArrayList<String> unincluded = new ArrayList<>();

        Lsbl serviceResponse = createDeviceListResponse(included, unincluded);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.TOPOSERVICE", serviceResponse);

        Lsbl response = topoServiceClient.getDeviceListAwait(gatewaySgtin);

        Lsbl sent = mock.get();
        assert sent.getCmd().getTopoCmd().getGwDeviceListGet().getGwSgtin().equals(gatewaySgtin);
        assert LsBL.isResponse(response);
    }

    @Test
    public void getDeviceDescription() throws Exception {
        String deviceSgtin = TestHelper.randomSgtin();
        String deviceMac = TestHelper.randomMac();
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        Lsbl serviceResponse = createDeviceDescriptionGetResponse(deviceSgtin, deviceMac);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.TOPOSERVICE", serviceResponse);

        topoServiceClient.getDeviceDescription(deviceSgtin, lsbl -> {
            result.complete(lsbl);
        });

        Lsbl sent = mock.get();
        assert sent.getCmd().getTopoCmd().getDeviceDescriptionGet().getDeviceSgtin().equals(deviceSgtin);

        Lsbl response = result.get();
        assert LsBL.isResponse(response);
    }

    @Test
    public void getDeviceDescriptionAwait() throws Exception {
        String deviceSgtin = TestHelper.randomSgtin();
        String deviceMac = TestHelper.randomMac();
        Lsbl serviceResponse = createDeviceDescriptionGetResponse(deviceSgtin, deviceMac);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.TOPOSERVICE", serviceResponse);

        Lsbl response = topoServiceClient.getDeviceDescriptionAwait(deviceSgtin);

        Lsbl sent = mock.get();
        assert sent.getCmd().getTopoCmd().getDeviceDescriptionGet().getDeviceSgtin().equals(deviceSgtin);

        assert LsBL.isResponse(response);
    }

    private static Lsbl createGatewayListResponse(int count) {
        TopoResponse topoResponse = new TopoResponse();
        GwListGetResponse gwListGetResponse = new GwListGetResponse();
        ArrayList<String> sgtins = new ArrayList<>();
        for(int i = 0; i < count; i++){
            sgtins.add(TestHelper.randomSgtin());
        }
        gwListGetResponse.getGw().addAll(sgtins);
        gwListGetResponse.setCount(sgtins.size());
        topoResponse.setGwListGet(gwListGetResponse);
        return createTopoResponse(topoResponse);
    }

    private static Lsbl createDeviceListResponse(List<String> includedDevicesLsdl, List<String> unincludedDevicesLsdl) {
        TopoResponse topoResponse = new TopoResponse();
        GwDeviceListGetResponse gwDeviceListGetResponse = new GwDeviceListGetResponse();
        DeviceList included = new DeviceList();
        included.getLsdl().addAll(includedDevicesLsdl);
        DeviceList unincluded = new DeviceList();
        unincluded.getLsdl().addAll(unincludedDevicesLsdl);
        gwDeviceListGetResponse.setDeviceList(included);
        gwDeviceListGetResponse.setUnincludedDeviceList(unincluded);
        topoResponse.setGwDeviceListGet(gwDeviceListGetResponse);
        return createTopoResponse(topoResponse);
    }

    private static Lsbl createDeviceDescriptionGetResponse(String deviceSgtin, String deviceMac) {
        TopoResponse topoResponse = new TopoResponse();
        DeviceDescriptionGetResponse deviceDescriptionGetResponse = new DeviceDescriptionGetResponse();
        String lsdl = TestHelper.createDeviceDescriptionReport(deviceSgtin, deviceMac);
        deviceDescriptionGetResponse.setLsdl(lsdl);
        topoResponse.setDeviceDescriptionGet(deviceDescriptionGetResponse);
        return createTopoResponse(topoResponse);
    }

    private static Lsbl createTopoResponse(TopoResponse topoResponse) {
        Lsbl lsbl = LsBL.create(null, null, 42, MessageType.LSBL_RESPONSE);
        Lsbl.Response response = new Lsbl.Response();
        response.setTopoResponse(topoResponse);
        lsbl.setResponse(response);
        return lsbl;
    }

}
