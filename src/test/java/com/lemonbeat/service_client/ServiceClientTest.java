package com.lemonbeat.service_client;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl_topo_service.GwListGetRequest;
import com.lemonbeat.lsbl.lsbl_topo_service.TopoCmd;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ServiceClientTest {

    @Test
    public void constructorWithConnection() {
        Connection connection = null;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            connection = factory.newConnection();
        } catch (Exception ex) {}

        ServiceClient serviceClient = new ServiceClient(connection);
        assert serviceClient.getConnection() == null;
        assert serviceClient.getSettings().isEmpty();
    }

    @Test
    public void constructorWithInvalidPropertiesFile() {
        try {
            new ServiceClient("BAD_FILE");
        } catch (Exception ex) {
            assertTrue(ex instanceof RuntimeException);
        }
    }

    @Test
    public void constructorWithValidPropertiesFile() {
        ServiceClient serviceClient = new ServiceClient("settings.properties");
        assert serviceClient.getSettings().keySet().size() == 12;
        assert serviceClient.getConnection().isOpen();
    }

    @Test
    public void constructorWithMetricsCollector() throws IOException, InterruptedException {
        ExampleMetricsCollector metricsCollector = new ExampleMetricsCollector();
        ServiceClient serviceClient = new ServiceClient("settings.properties", metricsCollector);
        assert metricsCollector.connectionCount == 1;
    }

    @Test
    public void subscribeShouldReceiveEvents() throws Exception {
        CompletableFuture<Lsbl> event = new CompletableFuture<>();
        ServiceClient serviceClient = new ServiceClient("settings.properties");

        String consumerTag = serviceClient.subscribe("EVENT.APP.SERVICE.EVENT_NAME", lsbl -> {
            event.complete(lsbl);
        });

        Lsbl expectedEvent = TestHelper.publishTestEvent(serviceClient.getConnection(), "EVENT.APP.SERVICE.EVENT_NAME");
        Lsbl lsbl = event.get(30, TimeUnit.SECONDS);
        assert consumerTag != null;
        assert LsBL.write(lsbl).equals(LsBL.write(expectedEvent));
    }

    @Test
    public void subscribeWithDurableQueue() throws Exception {
        String testEventName = "EVENT.APP.SERVICE.EVENT_NAME_" + TestHelper.randomUuid().toUpperCase();

        ServiceClient serviceClient = new ServiceClient("settings.properties");
        serviceClient.subscribe(testEventName, lsbl -> {}, true);
        serviceClient.getConnection().close();

        ServiceClient newServiceClient = new ServiceClient("settings.properties");
        Lsbl expectedEvent = TestHelper.publishTestEvent(newServiceClient.getConnection(), testEventName);

        CompletableFuture<Lsbl> event = new CompletableFuture<>();
        newServiceClient.subscribe(testEventName, lsbl -> {
            event.complete(lsbl);
        }, true);

        Lsbl lsbl = event.get(30, TimeUnit.SECONDS);
        assert LsBL.write(lsbl).equals(LsBL.write(expectedEvent));
    }

    @Test
    public void call() throws Exception {
        CompletableFuture<Lsbl> response = new CompletableFuture<>();
        ServiceClient serviceClient = new ServiceClient("settings.properties");

        Lsbl request = LsBL.create(null, "SERVICE.TOPOSERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        TopoCmd topoCmd = new TopoCmd();
        GwListGetRequest gwListGetRequest = new GwListGetRequest();
        topoCmd.setGwListGet(gwListGetRequest);
        cmd.setTopoCmd(topoCmd);
        request.setCmd(cmd);

        serviceClient.call(request, lsbl -> {
            response.complete(lsbl);
        });

        Lsbl lsblResponse = response.get(30, TimeUnit.SECONDS);
        assert LsBL.isNack(lsblResponse);
    }

    @Test
    public void callAwait() {
        ServiceClient serviceClient = new ServiceClient("settings.properties");

        Lsbl request = LsBL.create(null, "SERVICE.TOPOSERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        TopoCmd topoCmd = new TopoCmd();
        GwListGetRequest gwListGetRequest = new GwListGetRequest();
        topoCmd.setGwListGet(gwListGetRequest);
        cmd.setTopoCmd(topoCmd);
        request.setCmd(cmd);

        Lsbl lsblResponse = serviceClient.callAwait(request);
        assert LsBL.isNack(lsblResponse);
    }

    @Test
    public void tokenGetterAndSetter() {
        ServiceClient serviceClient = new ServiceClient("settings.properties");
        assert serviceClient.getToken() == null;
        serviceClient.setToken("SOME_TOKEN");
        assert serviceClient.getToken().equals("SOME_TOKEN");
    }

    @Test
    public void tokenExpiresGetterAndSetter() {
        ServiceClient serviceClient = new ServiceClient("settings.properties");
        assert serviceClient.getTokenExpires() == 0;
        serviceClient.setTokenExpires(1000);
        assert serviceClient.getTokenExpires() == 1000;
    }

    @Test
    public void getSettingsFromFile() {
        ServiceClient serviceClient = new ServiceClient("settings.properties");
        List<String> expectedSettings = Arrays.asList(
                "BROKER_HOST",
                "BROKER_VHOST",
                "BROKER_PORT",
                "BROKER_USERNAME",
                "BROKER_PASSWORD",
                "BACKEND_USERNAME",
                "BACKEND_PASSWORD",
                "TRUSTSTORE_PATH",
                "TRUSTSTORE_PASSWORD",
                "CLIENT_NAME",
                "CLIENT_P12_PATH",
                "CLIENT_P12_PASSWORD"
        );
        assert serviceClient.getSettings().keySet().containsAll(expectedSettings);
    }



    /*

    @Test
    public void callAwait() {
        ServiceClient serviceClient = new ServiceClient("settings.properties");
        // Create a gw_list_get message
        Lsbl lsbl = LsBL.create(null, "SERVICE.TOPOSERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        TopoCmd topoCmd = new TopoCmd();
        GwListGetRequest gwListGetRequest = new GwListGetRequest();
        topoCmd.setGwListGet(gwListGetRequest);
        cmd.setTopoCmd(topoCmd);
        lsbl.setCmd(cmd);

        Lsbl response = serviceClient.callAwait(lsbl);
        assert LsBL.isNack(response);
    }

    @Test
    public void call() {
        ServiceClient serviceClient = new ServiceClient("settings.properties");
        // Create a gw_list_get message
        Lsbl lsbl = LsBL.create(null, "SERVICE.TOPOSERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        TopoCmd topoCmd = new TopoCmd();
        GwListGetRequest gwListGetRequest = new GwListGetRequest();
        topoCmd.setGwListGet(gwListGetRequest);
        cmd.setTopoCmd(topoCmd);
        lsbl.setCmd(cmd);

        // Send this message multiple times and assert answers
        for(int i = 0; i < 10; i++){
            serviceClient.call(lsbl, response -> {
                assert LsBL.isNack(response);
            });
        }
    }
    */

}