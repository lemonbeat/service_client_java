package com.lemonbeat.service_client;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataAttribute;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataCmd;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataGetRequest;
import com.lemonbeat.lsbl.lsbl_metadata_service.MetadataSetRequest;

import java.util.List;

/**
 * ServiceClient for getting metadata for devices/gateways by SGTIN and UUID.
 */
public class MetadataServiceClient {

    private ServiceClient serviceClient;

    private static final String SERVICE_QUEUE = "SERVICE.METADATASERVICE";

    public MetadataServiceClient(ServiceClient serviceClient){
        this.serviceClient = serviceClient;
    }

    /**
     * Set the metadata for the given SGTIN.
     * @param sgtin SGTIN of the device/gateway
     * @param attributes List of MetadataAttributes that will be set.
     * @param callback Callback that will receive the response.
     */
    public void setMetadataBySgtin(String sgtin, List<MetadataAttribute> attributes, ServiceClient.ResponseCallback callback) {
        Lsbl cmd = createSetMetadataRequest(sgtin, null, attributes);
        serviceClient.call(cmd, callback);
    }

    /**
     * Set the metadata for the given SGTIN.
     * @param sgtin SGTIN of the device/gateway
     * @param attributes List of MetadataAttributes that will be set.
     * @return Response LsBL
     */
    public Lsbl setMetadataBySgtinAwait(String sgtin, List<MetadataAttribute> attributes) {
        Lsbl cmd = createSetMetadataRequest(sgtin, null, attributes);
        return serviceClient.callAwait(cmd);
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

    private Lsbl createSetMetadataRequest(String sgtin, String uuid, List<MetadataAttribute> attributes) {
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        MetadataCmd metadataCmd = new MetadataCmd();
        MetadataSetRequest metadataSetRequest = new MetadataSetRequest();
        metadataSetRequest.setSgtin(sgtin);
        metadataSetRequest.setUuid(uuid);
        metadataSetRequest.getAttribute().addAll(attributes);
        metadataCmd.setMetadataSet(metadataSetRequest);
        cmd.setMetadataCmd(metadataCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

    private Lsbl createGetMetadataBySgtinRequest(String sgtin) {
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        MetadataCmd metadataCmd = new MetadataCmd();
        MetadataGetRequest metadataGetRequest = new MetadataGetRequest();
        metadataGetRequest.setSgtin(sgtin);
        metadataCmd.setMetadataGet(metadataGetRequest);
        cmd.setMetadataCmd(metadataCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

    private Lsbl createGetMetadataByUuidRequest(String uuid) {
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        MetadataCmd metadataCmd = new MetadataCmd();
        MetadataGetRequest metadataGetRequest = new MetadataGetRequest();
        metadataGetRequest.setUuid(uuid);
        metadataCmd.setMetadataGet(metadataGetRequest);
        cmd.setMetadataCmd(metadataCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

}
