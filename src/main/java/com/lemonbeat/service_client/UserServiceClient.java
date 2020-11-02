package com.lemonbeat.service_client;

import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl_user_service.*;

import java.util.Properties;

/**
 * ServiceClient for login and token refresh.
 * When the requests are successful the token will be stored in the service client instance.
 */
public class UserServiceClient {

    private ServiceClient serviceClient;

    private static final String SERVICE_QUEUE = "SERVICE.USERSERVICE";

    public UserServiceClient(ServiceClient serviceClient){
        this.serviceClient = serviceClient;
    }

    /**
     * Login with the given credentials.
     * If the login succeeds the token will be automatically stored in the serviceClient instance.
     * @param username Backend username
     * @param password Backend password
     * @param callback Callback that will receive the response.
     */
    public void login(String username, String password, ServiceClient.ResponseCallback callback) {
        Lsbl cmd = createLoginRequest(username, password);
        serviceClient.call(cmd, response -> {
            if(LsBL.isResponse(response)){
                UserLoginResponse userLoginResponse = response.getResponse().getUserResponse().getUserLogin();
                serviceClient.setToken(userLoginResponse.getToken());
                serviceClient.setTokenExpires(userLoginResponse.getExpires());
            }
            callback.onResponse(response);
        });
    }

    /**
     * Login with the given credentials and return the response.
     * If the login succeeds the token will be automatically stored in the serviceClient instance.
     * @param username Backend username
     * @param password Backend password
     * @return Lsbl with the result of the login request.
     */
    public Lsbl loginAwait(String username, String password) {
        Lsbl cmd = createLoginRequest(username, password);
        Lsbl response = serviceClient.callAwait(cmd);
        if(LsBL.isResponse(response)){
            UserLoginResponse userLoginResponse = response.getResponse().getUserResponse().getUserLogin();
            serviceClient.setToken(userLoginResponse.getToken());
            serviceClient.setTokenExpires(userLoginResponse.getExpires());
        }
        return response;
    }

    /**
     * Login with BACKEND_USERNAME and BACKEND_PASSWORD from the settings.properties file.
     * @return Lsbl with the result of the login request.
     */
    public Lsbl loginAwait() {
        Properties settings = this.serviceClient.getSettings();
        String username = settings.getProperty("BACKEND_USERNAME", "");
        String password = settings.getProperty("BACKEND_PASSWORD", "");
        return loginAwait(username, password);
    }

    /**
     * The JWT expires, this method does a refresh and stores the new token in the serviceClient instance.
     * @param callback Callback that will receive the response.
     */
    public void tokenRefresh(ServiceClient.ResponseCallback callback) {
        Lsbl cmd = createRefreshRequest();

        serviceClient.call(cmd, response -> {
            if(LsBL.isResponse(response)){
                UserTokenRefreshResponse tokenRefreshResponse = response.getResponse().getUserResponse().getUserTokenRefresh();
                serviceClient.setToken(tokenRefreshResponse.getToken());
                serviceClient.setTokenExpires(tokenRefreshResponse.getExpires());
            }
            callback.onResponse(response);
        });
    }

    /**
     * The JWT expires, this method does a refresh and stores the new token in the serviceClient instance.
     * @return Lsbl with the result of the token refresh request.
     */
    public Lsbl tokenRefreshAwait() {
        Lsbl cmd = createRefreshRequest();
        Lsbl response = serviceClient.callAwait(cmd);
        if(LsBL.isResponse(response)){
            UserTokenRefreshResponse tokenRefreshResponse = response.getResponse().getUserResponse().getUserTokenRefresh();
            serviceClient.setToken(tokenRefreshResponse.getToken());
            serviceClient.setTokenExpires(tokenRefreshResponse.getExpires());
        }
        return response;
    }

    private Lsbl createRefreshRequest(){
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        UserCmd userCmd = new UserCmd();
        UserTokenRefreshRequest userTokenRefreshRequest = new UserTokenRefreshRequest();
        userTokenRefreshRequest.setToken(serviceClient.getToken());
        userCmd.setUserTokenRefresh(userTokenRefreshRequest);
        cmd.setUserCmd(userCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

    private Lsbl createLoginRequest(String username, String password) {
        Lsbl.Cmd cmd = new Lsbl.Cmd();
        UserCmd userCmd = new UserCmd();
        UserLoginRequest userLoginRequest = new UserLoginRequest();
        userLoginRequest.setUsername(username);
        userLoginRequest.setPassword(password);
        userCmd.setUserLogin(userLoginRequest);
        cmd.setUserCmd(userCmd);
        return LsBL.createCmd(cmd, SERVICE_QUEUE, serviceClient.getToken());
    }

}
