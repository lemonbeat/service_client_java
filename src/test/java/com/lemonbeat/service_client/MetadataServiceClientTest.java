package com.lemonbeat.service_client;
import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataAttribute;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

public class MetadataServiceClientTest {

    static ServiceClient serviceClient;
    static MetadataServiceClient metadataServiceClient;

    @Before
    public void setupMetadataServiceClient() {
        serviceClient = new ServiceClient("settings.properties");
        metadataServiceClient = new MetadataServiceClient(serviceClient);
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
    public void setMetadataBySgtinWithInvalidSgtin() throws Exception {
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        Lsbl nack = TestHelper.createNack();
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.METADATASERVICE", nack);

        metadataServiceClient.setMetadataBySgtin("INVALID", new ArrayList<MetadataAttribute>(), lsbl -> {
            result.complete(lsbl);
        });

        assert LsBL.isNack(result.get());
        Lsbl sent = mock.get();
        assert sent.getCmd().getMetadataCmd().getMetadataSet().getSgtin().equals("INVALID");
    }

    @Test
    public void setMetadataBySgtinWithValidSgtin() throws Exception {
        String randomSgtin = TestHelper.randomSgtin();
        CompletableFuture<Lsbl> result = new CompletableFuture<>();
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponseAck(serviceClient, "SERVICE.METADATASERVICE");

        ArrayList<MetadataAttribute> attributes = new ArrayList<>();

        MetadataAttribute a = new MetadataAttribute();
        a.setName("A");
        a.setValue("WORKS");
        attributes.add(a);


        metadataServiceClient.setMetadataBySgtin(randomSgtin, attributes, lsbl -> {
            result.complete(lsbl);
        });

        assert LsBL.isAck(result.get());
        Lsbl sent = mock.get();
        assert sent.getCmd().getMetadataCmd().getMetadataSet().getSgtin().equals(randomSgtin);
        assert sent.getCmd().getMetadataCmd().getMetadataSet().getAttribute().get(0).getName().equals("A");
        assert sent.getCmd().getMetadataCmd().getMetadataSet().getAttribute().get(0).getValue().equals("WORKS");
    }

    @Test
    public void setMetadataBySgtinAwaitWithInvalidSgtin() throws Exception {

        Lsbl nack = TestHelper.createNack();
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.METADATASERVICE", nack);

        Lsbl result = metadataServiceClient.setMetadataBySgtinAwait("INVALID", new ArrayList<MetadataAttribute>());

        assert LsBL.isNack(result);
        Lsbl sent = mock.get();
        assert sent.getCmd().getMetadataCmd().getMetadataSet().getSgtin().equals("INVALID");
    }

    @Test
    public void setMetadataBySgtinAwaitWithValidSgtin() throws Exception {
        String randomSgtin = TestHelper.randomSgtin();
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponseAck(serviceClient, "SERVICE.METADATASERVICE");

        ArrayList<MetadataAttribute> attributes = new ArrayList<>();

        MetadataAttribute a = new MetadataAttribute();
        a.setName("A");
        a.setValue("WORKS");
        attributes.add(a);


        Lsbl result = metadataServiceClient.setMetadataBySgtinAwait(randomSgtin, attributes);

        assert LsBL.isAck(result);
        Lsbl sent = mock.get();
        assert sent.getCmd().getMetadataCmd().getMetadataSet().getSgtin().equals(randomSgtin);
        assert sent.getCmd().getMetadataCmd().getMetadataSet().getAttribute().get(0).getName().equals("A");
        assert sent.getCmd().getMetadataCmd().getMetadataSet().getAttribute().get(0).getValue().equals("WORKS");
    }

    @Test
    public void getMetadataBySgtin() throws Exception {
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        Lsbl nack = TestHelper.createNack();
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.METADATASERVICE", nack);

        metadataServiceClient.getMetadataBySgtin("INVALID", lsbl -> {
            result.complete(lsbl);
        });

        assert LsBL.isNack(result.get());
        Lsbl sent = mock.get();
        assert sent.getCmd().getMetadataCmd().getMetadataGet().getSgtin().equals("INVALID");
    }

    @Test
    public void getMetadataBySgtinAwait() throws Exception {
        String randomSgtin = TestHelper.randomSgtin();

        Lsbl nack = TestHelper.createNack();
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.METADATASERVICE", nack);

        Lsbl result = metadataServiceClient.getMetadataBySgtinAwait(randomSgtin);

        assert LsBL.isNack(result);
        Lsbl sent = mock.get();
        assert sent.getCmd().getMetadataCmd().getMetadataGet().getSgtin().equals(randomSgtin);
    }

    @Test
    public void getMetadataByUuid() throws Exception {
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        Lsbl nack = TestHelper.createNack();
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.METADATASERVICE", nack);

        metadataServiceClient.getMetadataByUuid("INVALID", lsbl -> {
            result.complete(lsbl);
        });

        assert LsBL.isNack(result.get());
        Lsbl sent = mock.get();
        assert sent.getCmd().getMetadataCmd().getMetadataGet().getUuid().equals("INVALID");
    }

    @Test
    public void getMetadataByUuidAwait() throws Exception {
        String randomUuid = TestHelper.randomUuid();

        Lsbl nack = TestHelper.createNack();
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.METADATASERVICE", nack);

        Lsbl result = metadataServiceClient.getMetadataByUuidAwait(randomUuid);

        assert LsBL.isNack(result);
        Lsbl sent = mock.get();
        assert sent.getCmd().getMetadataCmd().getMetadataGet().getUuid().equals(randomUuid);
    }

}
