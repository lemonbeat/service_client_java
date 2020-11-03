package com.lemonbeat.service_client;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl_value_service.ValueCmd;
import com.lemonbeat.lsbl.lsbl_value_service.ValueDescriptionGetRequest;
import com.lemonbeat.lsbl.lsbl_value_service.ValueGetRequest;
import com.lemonbeat.lsbl.lsbl_value_service.ValueSetRequest;
import com.lemonbeat.lsdl.LsDL;
import com.lemonbeat.lsdl.value.ValueSetType;

import java.util.List;

/**
 * ServiceClient for getting and setting values on devices.
 */
public class ValueServiceClient {


    private ServiceClient serviceClient;

    private static final String SERVICE_QUEUE = "SERVICE.VALUESERVICE";

    public ValueServiceClient(ServiceClient serviceClient){
        this.serviceClient = serviceClient;
    }


    /**
     * Get the current value_report for the given device by SGTIN.
     * @param deviceSgtin SGTIN of the device
     * @param callback Callback that will receive the response.
     */
    public void getValuesBySgtin(String deviceSgtin, ServiceClient.ResponseCallback callback) {
        Lsbl cmd = createValueGetBySgtinRequest(deviceSgtin);
        serviceClient.call(cmd, callback);
    }

    /**
     * Get the current value_report for the given device by SGTIN and return the response.
     * @param deviceSgtin SGTIN of the device
     * @return Lsbl with the result of the request.
     */
    public Lsbl getValuesBySgtinAwait(String deviceSgtin) {
        Lsbl cmd = createValueGetBySgtinRequest(deviceSgtin);
        return serviceClient.callAwait(cmd);
    }

    /**
     * Get the current value_report for the given device by UUID.
     * @param deviceUuid UUID of the device
     * @param callback Callback that will receive the response.
     */
    public void getValuesByUuid(String deviceUuid, ServiceClient.ResponseCallback callback) {
        Lsbl cmd = createValueGetByUuidRequest(deviceUuid);
        serviceClient.call(cmd, callback);
    }

    /**
     * Get the current value_report for the given device by UUID and return the response.
     * @param deviceUuid UUID of the device
     * @return Lsbl with the result of the request.
     */
    public Lsbl getValuesByUuidAwait(String deviceUuid) {
        Lsbl cmd = createValueGetByUuidRequest(deviceUuid);
        return serviceClient.callAwait(cmd);
    }


    /**
     * Get the current value_description_report for the given device by SGTIN.
     * @param deviceSgtin SGTIN of the device
     * @param callback Callback that will receive the response.
     */
    public void getValueDescriptionBySgtin(String deviceSgtin, ServiceClient.ResponseCallback callback) {
        Lsbl cmd = createValueDescriptionGetBySgtinRequest(deviceSgtin);
        serviceClient.call(cmd, callback);
    }

    /**
     * Get the current value_description_report for the given device by SGTIN and return the response.
     * @param deviceSgtin SGTIN of the device
     * @return Lsbl with the result of the request.
     */
    public Lsbl getValueDescriptionBySgtinAwait(String deviceSgtin) {
        Lsbl cmd = createValueDescriptionGetBySgtinRequest(deviceSgtin);
        return serviceClient.callAwait(cmd);
    }

    /**
     * Get the current value_description_report for the given device by UUID.
     * @param deviceUuid UUID of the device
     * @param callback Callback that will receive the response.
     */
    public void getValueDescriptionByUuid(String deviceUuid, ServiceClient.ResponseCallback callback) {
        Lsbl cmd = createValueDescriptionGetByUuidRequest(deviceUuid);
        serviceClient.call(cmd, callback);
    }

    /**
     * Get the current value_description_report for the given device by UUID and return the response.
     * @param deviceUuid UUID of the device
     * @return Lsbl with the result of the request.
     */
    public Lsbl getValueDescriptionByUuidAwait(String deviceUuid) {
        Lsbl cmd = createValueDescriptionGetByUuidRequest(deviceUuid);
        return serviceClient.callAwait(cmd);
    }


    /**
     * Set values for the given device by SGTIN.
     * @param deviceSgtin SGTIN of the device
     * @param values List of values that should be set
     * @param callback Callback that will receive the response.
     */
    public void setValueBySgtin(String deviceSgtin, List<ValueSetType> values, ServiceClient.ResponseCallback callback) {
        this.setValueBySgtin(deviceSgtin, values, 0, callback);
    }

    /**
     * Set values for the given device by SGTIN, with retries. This command ensures the values have been set.
     * @param deviceSgtin SGTIN of the device
     * @param values List of values that should be set
     * @param retries Number of retries
     * @param callback Callback that will receive the response.
     */
    public void setValueBySgtin(String deviceSgtin, List<ValueSetType> values, long retries, ServiceClient.ResponseCallback callback) {
        Lsbl cmd = createValueSetBySgtin(deviceSgtin, values, retries);
        serviceClient.call(cmd, callback);
    }


    /**
     * Set values for the given device by SGTIN and return the response.
     * @param deviceSgtin SGTIN of the device
     * @param values List of values that should be set
     * @return Lsbl with the result of the request.
     */
    public Lsbl setValueBySgtinAwait(String deviceSgtin, List<ValueSetType> values){
        return this.setValueBySgtinAwait(deviceSgtin, values, 0);
    }

    /**
     * Set values for the given device by SGTIN with retries and return the response.
     * @param deviceSgtin SGTIN of the device
     * @param values List of values that should be set
     * @param retries Number of retries
     * @return Lsbl with the result of the request.
     */
    public Lsbl setValueBySgtinAwait(String deviceSgtin, List<ValueSetType> values, long retries){
        Lsbl cmd = createValueSetBySgtin(deviceSgtin, values, retries);
        return serviceClient.callAwait(cmd);
    }

    /**
     * Set values for the given device by UUID.
     * @param deviceUuid UUID of the device
     * @param values List of values that should be set
     * @param callback Callback that will receive the response.
     */
    public void setValueByUuid(String deviceUuid, List<ValueSetType> values, ServiceClient.ResponseCallback callback){
        setValueByUuid(deviceUuid, values, 0, callback);
    }

    /**
     * Set values for the given device by UUID, with retries. This command ensures the values have been set.
     * @param deviceUuid UUID of the device
     * @param values List of values that should be set
     * @param retries Number of retries
     * @param callback Callback that will receive the response.
     */
    public void setValueByUuid(String deviceUuid, List<ValueSetType> values, long retries, ServiceClient.ResponseCallback callback){
        Lsbl cmd = createValueSetByUuid(deviceUuid, values, retries);
        serviceClient.call(cmd, callback);
    }

    /**
     * Set values for the given device by UUID and return the response.
     * @param deviceUuid UUID of the device
     * @param values List of values that should be set
     * @return Lsbl with the result of the request.
     */
    public Lsbl setValueByUuidAwait(String deviceUuid, List<ValueSetType> values){
        return setValueByUuidAwait(deviceUuid, values, 0);
    }

    /**
     * Set values for the given device by UUID with retries and return the response.
     * @param deviceUuid UUID of the device
     * @param values List of values that should be set
     * @param retries Number of retries
     * @return Lsbl with the result of the request.
     */
    public Lsbl setValueByUuidAwait(String deviceUuid, List<ValueSetType> values, long retries){
        Lsbl cmd = createValueSetByUuid(deviceUuid, values, retries);
        return serviceClient.callAwait(cmd);
    }

    private Lsbl createValueGetBySgtinRequest(String deviceSgtin){
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        ValueCmd valueCmd = new ValueCmd();
        ValueGetRequest valueGetRequest = new ValueGetRequest();
        valueGetRequest.setDeviceSgtin(deviceSgtin);
        valueCmd.setValueGet(valueGetRequest);
        cmd.setValueCmd(valueCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

    private Lsbl createValueGetByUuidRequest(String deviceUuid){
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        ValueCmd valueCmd = new ValueCmd();
        ValueGetRequest valueGetRequest = new ValueGetRequest();
        valueGetRequest.setDeviceUuid(deviceUuid);
        valueCmd.setValueGet(valueGetRequest);
        cmd.setValueCmd(valueCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

    private Lsbl createValueDescriptionGetBySgtinRequest(String deviceSgtin){
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        ValueCmd valueCmd = new ValueCmd();
        ValueDescriptionGetRequest valueDescriptionGetRequest = new ValueDescriptionGetRequest();
        valueDescriptionGetRequest.setDeviceSgtin(deviceSgtin);
        valueCmd.setValueDescriptionGet(valueDescriptionGetRequest);
        cmd.setValueCmd(valueCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

    private Lsbl createValueDescriptionGetByUuidRequest(String deviceUuid){
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        ValueCmd valueCmd = new ValueCmd();
        ValueDescriptionGetRequest valueDescriptionGetRequest = new ValueDescriptionGetRequest();
        valueDescriptionGetRequest.setDeviceUuid(deviceUuid);
        valueCmd.setValueDescriptionGet(valueDescriptionGetRequest);
        cmd.setValueCmd(valueCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

    private Lsbl createValueSetBySgtin(String deviceSgtin, List<ValueSetType> values, long retries){
        String lsdl = LsDL.write(LsDL.createValueSet(values));
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        ValueCmd valueCmd = new ValueCmd();
        ValueSetRequest valueSetRequest = new ValueSetRequest();
        valueSetRequest.setDeviceSgtin(deviceSgtin);
        valueSetRequest.setRetries(retries);
        valueSetRequest.setLsdl(lsdl);
        valueCmd.setValueSet(valueSetRequest);
        cmd.setValueCmd(valueCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

    private Lsbl createValueSetByUuid(String deviceUuid, List<ValueSetType> values, long retries){
        String lsdl = LsDL.write(LsDL.createValueSet(values));
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        ValueCmd valueCmd = new ValueCmd();
        ValueSetRequest valueSetRequest = new ValueSetRequest();
        valueSetRequest.setDeviceUuid(deviceUuid);
        valueSetRequest.setRetries(retries);
        valueSetRequest.setLsdl(lsdl);
        valueCmd.setValueSet(valueSetRequest);
        cmd.setValueCmd(valueCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

}
