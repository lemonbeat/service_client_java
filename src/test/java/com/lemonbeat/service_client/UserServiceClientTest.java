package com.lemonbeat.service_client;
import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl.MessageType;
import com.lemonbeat.lsbl.lsbl_user_service.UserLoginResponse;
import com.lemonbeat.lsbl.lsbl_user_service.UserResponse;
import com.lemonbeat.lsbl.lsbl_user_service.UserTokenRefreshResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

public class UserServiceClientTest {

    static ServiceClient serviceClient;
    static UserServiceClient userServiceClient;

    @Before
    public void setupUserServiceClient() throws InterruptedException {
        serviceClient = new ServiceClient("settings.properties");
        userServiceClient = new UserServiceClient(serviceClient);
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
    public void loginWithInvalidCredentials() throws Exception {
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        Lsbl nackResponse = TestHelper.createNack("Login failed, invalid credentials");
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.USERSERVICE", nackResponse);

        userServiceClient.login("INVALID", "CREDENTIALS", lsbl -> {
            result.complete(lsbl);
        });

        Lsbl sent = mock.get();
        assert sent.getCmd().getUserCmd().getUserLogin().getUsername().equals("INVALID");
        assert sent.getCmd().getUserCmd().getUserLogin().getPassword().equals("CREDENTIALS");

        Lsbl response = result.get();
        assert LsBL.isNack(response);
        assertEquals("Login failed, invalid credentials", response.getResponse().getCommonResponse().getNack().getMessage());
    }

    @Test
    public void loginWithValidCredentials() throws Exception {
        String username = serviceClient.getSettings().getProperty("BACKEND_USERNAME");
        String password = serviceClient.getSettings().getProperty("BACKEND_PASSWORD");
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        Lsbl loginResponse = createLoginResponse("SOMEJWTTOKEN");
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.USERSERVICE", loginResponse);

        userServiceClient.login(username, password, lsbl -> {
            result.complete(lsbl);
        });

        Lsbl sent = mock.get();
        assert sent.getCmd().getUserCmd().getUserLogin().getUsername().equals(username);
        assert sent.getCmd().getUserCmd().getUserLogin().getPassword().equals(password);

        Lsbl response = result.get();
        assert LsBL.isResponse(response);

        String expectedToken = response.getResponse().getUserResponse().getUserLogin().getToken();
        long expectedExpires = response.getResponse().getUserResponse().getUserLogin().getExpires();
        assertEquals(expectedToken, serviceClient.getToken());
        assertEquals(expectedExpires, serviceClient.getTokenExpires());
    }

    @Test
    public void loginAwaitWithInvalidCredentials() throws Exception {
        Lsbl nackResponse = TestHelper.createNack("Login failed, invalid credentials");
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.USERSERVICE", nackResponse);

        Lsbl response = userServiceClient.loginAwait("INVALID", "CREDENTIALS");
        Lsbl sent = mock.get();
        assert sent.getCmd().getUserCmd().getUserLogin().getUsername().equals("INVALID");
        assert sent.getCmd().getUserCmd().getUserLogin().getPassword().equals("CREDENTIALS");

        assert LsBL.isNack(response);
        assertEquals("Login failed, invalid credentials", response.getResponse().getCommonResponse().getNack().getMessage());
    }

    @Test
    public void loginAwaitWithValidCredentials() throws Exception {
        String username = serviceClient.getSettings().getProperty("BACKEND_USERNAME");
        String password = serviceClient.getSettings().getProperty("BACKEND_PASSWORD");

        Lsbl loginResponse = createLoginResponse("SOMEJWTTOKEN");
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.USERSERVICE", loginResponse);
        Lsbl response = userServiceClient.loginAwait(username, password);

        Lsbl sent = mock.get();
        assert sent.getCmd().getUserCmd().getUserLogin().getUsername().equals(username);
        assert sent.getCmd().getUserCmd().getUserLogin().getPassword().equals(password);


        assert LsBL.isResponse(response);
        String expectedToken = response.getResponse().getUserResponse().getUserLogin().getToken();
        long expectedExpires = response.getResponse().getUserResponse().getUserLogin().getExpires();
        assertEquals(expectedToken, serviceClient.getToken());
        assertEquals(expectedExpires, serviceClient.getTokenExpires());
    }

    @Test
    public void loginAwaitWithoutParameters() throws Exception {
        Lsbl loginResponse = createLoginResponse("SOMEJWTTOKEN");
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.USERSERVICE", loginResponse);
        Lsbl response = userServiceClient.loginAwait();

        assert LsBL.isResponse(response);
        String expectedToken = response.getResponse().getUserResponse().getUserLogin().getToken();
        long expectedExpires = response.getResponse().getUserResponse().getUserLogin().getExpires();
        assertEquals(expectedToken, serviceClient.getToken());
        assertEquals(expectedExpires, serviceClient.getTokenExpires());

        String username = serviceClient.getSettings().getProperty("BACKEND_USERNAME");
        String password = serviceClient.getSettings().getProperty("BACKEND_PASSWORD");
        Lsbl sentCommand = mock.get();
        assert sentCommand.getCmd().getUserCmd().getUserLogin().getUsername().equals(username);
        assert sentCommand.getCmd().getUserCmd().getUserLogin().getPassword().equals(password);
    }

    @Test
    public void tokenRefreshWithoutToken() throws Exception {
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.USERSERVICE", TestHelper.createNack());

        CompletableFuture<Lsbl> result = new CompletableFuture<>();
        userServiceClient.tokenRefresh(lsbl -> {
            result.complete(lsbl);
        });
        Lsbl response = result.get();

        Lsbl sent = mock.get();
        assert sent.getCmd().getUserCmd().getUserTokenRefresh().getToken() == null;
        assert LsBL.isNack(response);
    }

    @Test
    public void tokenRefreshWithToken() throws Exception {
        serviceClient.setToken("SOME_TOKEN");
        String oldToken = serviceClient.getToken();

        Lsbl refreshResponse = createTokenRefreshResponse("NEWTOKEN", 42);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.USERSERVICE", refreshResponse);

        CompletableFuture<Lsbl> result = new CompletableFuture<>();
        userServiceClient.tokenRefresh(lsbl -> {
            result.complete(lsbl);
        });
        Lsbl response = result.get();

        Lsbl sent = mock.get();
        assert sent.getCmd().getUserCmd().getUserTokenRefresh().getToken().equals("SOME_TOKEN");

        assert LsBL.isResponse(response);
        assertNotEquals(oldToken, serviceClient.getToken());
    }

    @Test
    public void tokenRefreshAwaitWithoutToken() throws Exception {
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.USERSERVICE", TestHelper.createNack());
        Lsbl response = userServiceClient.tokenRefreshAwait();
        assert LsBL.isNack(response);

        Lsbl sent = mock.get();
        assert sent.getCmd().getUserCmd().getUserTokenRefresh().getToken() == null;
    }

    @Test
    public void tokenRefreshAwaitWithToken() throws Exception {
        serviceClient.setToken("SOME_TOKEN");
        String oldToken = serviceClient.getToken();

        Lsbl refreshResponse = createTokenRefreshResponse("NEWTOKEN", 42);
        CompletableFuture<Lsbl> mock = TestHelper.mockServiceResponse(serviceClient, "SERVICE.USERSERVICE", refreshResponse);
        Lsbl response = userServiceClient.tokenRefreshAwait();

        Lsbl sent = mock.get();
        assert sent.getCmd().getUserCmd().getUserTokenRefresh().getToken().equals("SOME_TOKEN");

        assert LsBL.isResponse(response);
        assertNotEquals(oldToken, serviceClient.getToken());
    }

    private static Lsbl createLoginResponse(String token) {
        Lsbl lsbl = LsBL.create(null, null, 42, MessageType.LSBL_RESPONSE);
        Lsbl.Response response = new Lsbl.Response();
        UserResponse userResponse = new UserResponse();
        UserLoginResponse userLoginResponse = new UserLoginResponse();
        userLoginResponse.setToken(token);
        userLoginResponse.setExpires(Calendar.getInstance().getTimeInMillis() / 1000 + 3600);
        userResponse.setUserLogin(userLoginResponse);
        response.setUserResponse(userResponse);
        lsbl.setResponse(response);
        return lsbl;
    }

    private static Lsbl createTokenRefreshResponse(String token, long expires) {
        Lsbl lsbl = LsBL.create(null, null, 42, MessageType.LSBL_RESPONSE);
        Lsbl.Response response = new Lsbl.Response();
        UserResponse userResponse = new UserResponse();
        UserTokenRefreshResponse userTokenRefreshResponse = new UserTokenRefreshResponse();
        userTokenRefreshResponse.setToken(token);
        userTokenRefreshResponse.setExpires(expires);
        userResponse.setUserTokenRefresh(userTokenRefreshResponse);
        response.setUserResponse(userResponse);
        lsbl.setResponse(response);
        return lsbl;
    }

}
