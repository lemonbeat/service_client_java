package com.lemonbeat.service_client;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl_topo_service.DeviceDescriptionGetRequest;
import com.lemonbeat.lsbl.lsbl_topo_service.GwDeviceListGetRequest;
import com.lemonbeat.lsbl.lsbl_topo_service.GwListGetRequest;
import com.lemonbeat.lsbl.lsbl_topo_service.TopoCmd;

/**
 * ServiceClient for getting information about devices and gateways.
 */
public class TopoServiceClient {

    private ServiceClient serviceClient;

    private static final String SERVICE_QUEUE = "SERVICE.TOPOSERVICE";

    public TopoServiceClient(ServiceClient serviceClient){
        this.serviceClient = serviceClient;
    }


    /**
     * Get a list of all known gateways.
     * @param callback Callback that will receive the response.
     */
    public void getGatewayList(ServiceClient.ResponseCallback callback) {
        Lsbl cmd = createGwListGetRequest();
        serviceClient.call(cmd, callback);
    }

    /**
     * Get a list of all known gateways.
     * @return Lsbl with the result of the request.
     */
    public Lsbl getGatewayListAwait() {
        Lsbl cmd = createGwListGetRequest();
        return serviceClient.callAwait(cmd);
    }

    /**
     * Get all devices that are known by the given gateway.
     * @param gatewaySgtin SGTIN of the gateway.
     * @param callback Callback that will receive the response.
     */
    public void getDeviceList(String gatewaySgtin, ServiceClient.ResponseCallback callback) {
        Lsbl cmd = createGwDeviceListGetRequest(gatewaySgtin);
        serviceClient.call(cmd, callback);
    }

    /**
     * Get all devices that are known by the given gateway.
     * @param gatewaySgtin SGTIN of the gateway.
     * @return Lsbl with the result of the request.
     */
    public Lsbl getDeviceListAwait(String gatewaySgtin) {
        Lsbl cmd = createGwDeviceListGetRequest(gatewaySgtin);
        return serviceClient.callAwait(cmd);
    }


    /**
     * Get the device_description_report for the given device SGTIN.
     * @param deviceSgtin SGTIN of the device
     * @param callback Callback that will receive the response.
     */
    public void getDeviceDescription(String deviceSgtin, ServiceClient.ResponseCallback callback) {
        Lsbl cmd = createDeviceDescriptionGetRequest(deviceSgtin);
        serviceClient.call(cmd, callback);
    }

    /**
     * Get the device_description_report for the given device SGTIN.
     * @param deviceSgtin SGTIN of the device
     * @return Lsbl with the result of the request.
     */
    public Lsbl getDeviceDescriptionAwait(String deviceSgtin) {
        Lsbl cmd = createDeviceDescriptionGetRequest(deviceSgtin);
        return serviceClient.callAwait(cmd);
    }

    private Lsbl createGwListGetRequest() {
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        TopoCmd topoCmd = new TopoCmd();
        GwListGetRequest gwListGetRequest = new GwListGetRequest();
        topoCmd.setGwListGet(gwListGetRequest);
        cmd.setTopoCmd(topoCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

    private Lsbl createGwDeviceListGetRequest(String gatewaySgtin) {
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        TopoCmd topoCmd = new TopoCmd();
        GwDeviceListGetRequest deviceListGetRequest = new GwDeviceListGetRequest();
        deviceListGetRequest.setGwSgtin(gatewaySgtin);
        topoCmd.setGwDeviceListGet(deviceListGetRequest);
        cmd.setTopoCmd(topoCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

    private Lsbl createDeviceDescriptionGetRequest(String deviceSgtin) {
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        TopoCmd topoCmd = new TopoCmd();
        DeviceDescriptionGetRequest deviceDescriptionGetRequest = new DeviceDescriptionGetRequest();
        deviceDescriptionGetRequest.setDeviceSgtin(deviceSgtin);
        topoCmd.setDeviceDescriptionGet(deviceDescriptionGetRequest);
        cmd.setTopoCmd(topoCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

}
