package com.lemonbeat;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataCmd;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataGetRequest;

/**
 * This class implements getting metadata for devices/gateways by SGTIN and UUID.
 */
public class MetadataServiceClient {

    private ServiceClient serviceClient;

    private static final String SERVICE_QUEUE = "SERVICE.METADATASERVICE";

    public MetadataServiceClient(ServiceClient serviceClient){
        this.serviceClient = serviceClient;
    }

    /**
     * Get the metadata for the given device/gateway by SGTIN.
     * @param sgtin SGTIN of the device/gateway
     * @param callback Callback that will receive the response.
     */
    public void getMetadataBySgtin(String sgtin, ServiceClient.ResponseCallback callback){
        Lsbl cmd = createGetMetadataBySgtinRequest(sgtin);
        serviceClient.call(cmd, callback);
    }

    /**
     * Get the metadata for the given device/gateway by SGTIN and return the response.
     * @param sgtin SGTIN of the device/gateway
     * @return Lsbl with the result of the request.
     */
    public Lsbl getMetadataBySgtinAwait(String sgtin){
        Lsbl cmd = createGetMetadataBySgtinRequest(sgtin);
        return serviceClient.callAwait(cmd);
    }

    /**
     * Get the metadata for the given device/gateway by UUID.
     * @param uuid UUID of the device/gateway
     * @param callback Callback that will receive the response.
     */
    public void getMetadataByUuid(String uuid, ServiceClient.ResponseCallback callback){
        Lsbl cmd = createGetMetadataByUuidRequest(uuid);
        serviceClient.call(cmd, callback);
    }

    /**
     * Get the metadata for the given device/gateway by UUID and return the response.
     * @param uuid UUID of the device/gateway
     * @return Lsbl with the result of the request.
     */
    public Lsbl getMetadataByUuidAwait(String uuid){
        Lsbl cmd = createGetMetadataByUuidRequest(uuid);
        return serviceClient.callAwait(cmd);
    }

    private Lsbl createGetMetadataBySgtinRequest(String sgtin){
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        MetadataCmd metadataCmd = new MetadataCmd();
        MetadataGetRequest metadataGetRequest = new MetadataGetRequest();
        metadataGetRequest.setSgtin(sgtin);
        metadataCmd.setMetadataGet(metadataGetRequest);
        cmd.setMetadataCmd(metadataCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

    private Lsbl createGetMetadataByUuidRequest(String uuid){
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        MetadataCmd metadataCmd = new MetadataCmd();
        MetadataGetRequest metadataGetRequest = new MetadataGetRequest();
        metadataGetRequest.setUuid(uuid);
        metadataCmd.setMetadataGet(metadataGetRequest);
        cmd.setMetadataCmd(metadataCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

}
