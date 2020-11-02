package com.lemonbeat.service_client;
import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataAttribute;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class MetadataServiceClientTest {

    static ServiceClient serviceClient;
    static MetadataServiceClient metadataServiceClient;

    @Before
    public void setupMetadataServiceClient() {
        serviceClient = new ServiceClient("settings.properties");
        UserServiceClient userServiceClient = new UserServiceClient(serviceClient);
        userServiceClient.loginAwait();
        metadataServiceClient = new MetadataServiceClient(serviceClient);
    }

    @Test
    public void setMetadataBySgtin() throws ExecutionException, InterruptedException {
        CompletableFuture<Lsbl> result = new CompletableFuture<>();
        String sgtin = TestHelper.randomSgtin();
        ArrayList<MetadataAttribute> attributeList = new ArrayList<>();
        attributeList.add(TestHelper.createMetadataAttribute("A1", "42"));
        attributeList.add(TestHelper.createMetadataAttribute("A2", "works"));

        metadataServiceClient.setMetadataBySgtin(sgtin, attributeList, lsbl -> {
            result.complete(lsbl);
        });

        Lsbl response = result.get();
        assert LsBL.isAck(response);

        Lsbl metadataGetResponse = metadataServiceClient.getMetadataBySgtinAwait(sgtin);
        assert LsBL.isResponse(metadataGetResponse);

        List<MetadataAttribute> attributes = metadataGetResponse.getResponse().getMetadataResponse().getMetadataReport().getAttribute();
        assert attributes.size() == 2;
    }

    @Test
    public void getMetadataWithInvalidSgtin() throws ExecutionException, InterruptedException {
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        metadataServiceClient.getMetadataBySgtin("INVALID", lsbl -> {
            result.complete(lsbl);
        });

        Lsbl response = result.get();
        assert LsBL.isNack(response);
        assertEquals("Device with SGTIN INVALID unknown", response.getResponse().getCommonResponse().getNack().getMessage());
    }

    @Test
    public void getMetadataAwaitWithInvalidSgtin() throws ExecutionException, InterruptedException {
        Lsbl response = metadataServiceClient.getMetadataBySgtinAwait("INVALID");
        assert LsBL.isNack(response);
        assertEquals("Device with SGTIN INVALID unknown", response.getResponse().getCommonResponse().getNack().getMessage());
    }

    @Test
    public void getMetadataWithInvalidUuid() throws ExecutionException, InterruptedException {
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        metadataServiceClient.getMetadataByUuid("INVALID", lsbl -> {
            result.complete(lsbl);
        });

        Lsbl response = result.get();
        assert LsBL.isNack(response);
        assertEquals("Device with UUID INVALID unknown", response.getResponse().getCommonResponse().getNack().getMessage());
    }

    @Test
    public void getMetadataAwaitWithInvalidUuid() throws ExecutionException, InterruptedException {
        Lsbl response = metadataServiceClient.getMetadataByUuidAwait("INVALID");
        assert LsBL.isNack(response);
        assertEquals("Device with UUID INVALID unknown", response.getResponse().getCommonResponse().getNack().getMessage());
    }




}
