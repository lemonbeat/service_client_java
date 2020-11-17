package com.lemonbeat.service_client;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl_topo_service.GwListGetRequest;
import com.lemonbeat.lsbl.lsbl_topo_service.TopoCmd;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ServiceClientTest {

    static ServiceClient serviceClient;

    @After
    public void cleanup() {
        try {
            if(serviceClient != null && serviceClient.getConnection() != null){
                serviceClient.getConnection().close();
            }
        } catch (Exception e) {}
    }
    @Test
    public void constructorWithConnection() {
        Connection connection = null;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            connection = factory.newConnection();
        } catch (Exception ex) {}

        serviceClient = new ServiceClient(connection);
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
        serviceClient = new ServiceClient("settings.properties");
        assert serviceClient.getSettings().keySet().size() == 13;
        assert serviceClient.getConnection().isOpen();
    }

    @Test
    public void constructorWithMetricsCollector() throws IOException, InterruptedException {
        ExampleMetricsCollector metricsCollector = new ExampleMetricsCollector();
        serviceClient = new ServiceClient("settings.properties", metricsCollector);
        assert metricsCollector.connectionCount == 1;
    }

    @Test
    public void subscribeShouldReceiveEvents() throws Exception {
        CompletableFuture<Lsbl> event = new CompletableFuture<>();
        serviceClient = new ServiceClient("settings.properties");

        String consumerTag = serviceClient.subscribe("EVENT.APP.SERVICE.EVENT_NAME", lsbl -> {
            event.complete(lsbl);
        });

        Lsbl expectedEvent = TestHelper.publishTestEvent(serviceClient, "EVENT.APP.SERVICE.EVENT_NAME");
        Lsbl lsbl = event.get(30, TimeUnit.SECONDS);
        assert consumerTag != null;
        assert LsBL.write(lsbl).equals(LsBL.write(expectedEvent));
    }

    @Test
    public void subscribeWithDurableQueue() throws Exception {
        String testEventName = "EVENT.APP.SERVICE.EVENT_NAME_" + TestHelper.randomUuid().toUpperCase();

        serviceClient = new ServiceClient("settings.properties");
        serviceClient.subscribe(testEventName, lsbl -> {}, true);
        serviceClient.getConnection().close();

        ServiceClient newServiceClient = new ServiceClient("settings.properties");
        Lsbl expectedEvent = TestHelper.publishTestEvent(newServiceClient, testEventName);

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
        serviceClient = new ServiceClient("settings.properties");

        Lsbl request = LsBL.create(null, "SERVICE.TOPOSERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        TopoCmd topoCmd = new TopoCmd();
        GwListGetRequest gwListGetRequest = new GwListGetRequest();
        topoCmd.setGwListGet(gwListGetRequest);
        cmd.setTopoCmd(topoCmd);
        request.setCmd(cmd);

        TestHelper.mockServiceResponseAck(serviceClient, "SERVICE.TOPOSERVICE");
        serviceClient.call(request, lsbl -> {
            response.complete(lsbl);
        });

        Lsbl lsblResponse = response.get(30, TimeUnit.SECONDS);
        assert LsBL.isAck(lsblResponse);
    }

    @Test
    public void callAwait() throws Exception {
        serviceClient = new ServiceClient("settings.properties");

        Lsbl request = LsBL.create(null, "SERVICE.TOPOSERVICE", 0);
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        TopoCmd topoCmd = new TopoCmd();
        GwListGetRequest gwListGetRequest = new GwListGetRequest();
        topoCmd.setGwListGet(gwListGetRequest);
        cmd.setTopoCmd(topoCmd);
        request.setCmd(cmd);

        TestHelper.mockServiceResponseAck(serviceClient, "SERVICE.TOPOSERVICE");
        Lsbl lsblResponse = serviceClient.callAwait(request);
        assert LsBL.isAck(lsblResponse);
    }

    @Test
    public void tokenGetterAndSetter() {
        serviceClient = new ServiceClient("settings.properties");
        assert serviceClient.getToken() == null;
        serviceClient.setToken("SOME_TOKEN");
        assert serviceClient.getToken().equals("SOME_TOKEN");
    }

    @Test
    public void tokenExpiresGetterAndSetter() {
        serviceClient = new ServiceClient("settings.properties");
        assert serviceClient.getTokenExpires() == 0;
        serviceClient.setTokenExpires(1000);
        assert serviceClient.getTokenExpires() == 1000;
    }

    @Test
    public void getSettingsFromFile() {
        serviceClient = new ServiceClient("settings.properties");
        List<String> expectedSettings = Arrays.asList(
                "BROKER_HOST",
                "BROKER_VHOST",
                "BROKER_PORT",
                "BROKER_USERNAME",
                "BROKER_PASSWORD",
                "BROKER_SSL",
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

}