package com.lemonbeat.service_client;
import com.lemonbeat.lsbl.LsBL;
import com.lemonbeat.lsbl.lsbl.Lsbl;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class UserServiceClientTest {

    static ServiceClient serviceClient;
    static UserServiceClient userServiceClient;

    @Before
    public void setupUserServiceClient() {
        serviceClient = new ServiceClient("settings.properties");
        userServiceClient = new UserServiceClient(serviceClient);
    }

    @Test
    public void loginWithInvalidCredentials() throws ExecutionException, InterruptedException {
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        userServiceClient.login("INVALID", "CREDENTIALS", lsbl -> {
            result.complete(lsbl);
        });

        Lsbl response = result.get();
        assert LsBL.isNack(response);
        assertEquals("Login failed, invalid credentials", response.getResponse().getCommonResponse().getNack().getMessage());
    }

    @Test
    public void loginWithValidCredentials() throws ExecutionException, InterruptedException {
        String username = serviceClient.getSettings().getProperty("BACKEND_USERNAME");
        String password = serviceClient.getSettings().getProperty("BACKEND_PASSWORD");
        CompletableFuture<Lsbl> result = new CompletableFuture<>();

        userServiceClient.login(username, password, lsbl -> {
            result.complete(lsbl);
        });

        Lsbl response = result.get();
        assert LsBL.isResponse(response);

        String expectedToken = response.getResponse().getUserResponse().getUserLogin().getToken();
        long expectedExpires = response.getResponse().getUserResponse().getUserLogin().getExpires();
        assertEquals(expectedToken, serviceClient.getToken());
        assertEquals(expectedExpires, serviceClient.getTokenExpires());
    }

    @Test
    public void loginAwaitWithInvalidCredentials() {
        Lsbl response = userServiceClient.loginAwait("INVALID", "CREDENTIALS");
        assert LsBL.isNack(response);
        assertEquals("Login failed, invalid credentials", response.getResponse().getCommonResponse().getNack().getMessage());
    }

    @Test
    public void loginAwaitWithValidCredentials() {
        String username = serviceClient.getSettings().getProperty("BACKEND_USERNAME");
        String password = serviceClient.getSettings().getProperty("BACKEND_PASSWORD");

        Lsbl response = userServiceClient.loginAwait(username, password);

        assert LsBL.isResponse(response);
        String expectedToken = response.getResponse().getUserResponse().getUserLogin().getToken();
        long expectedExpires = response.getResponse().getUserResponse().getUserLogin().getExpires();
        assertEquals(expectedToken, serviceClient.getToken());
        assertEquals(expectedExpires, serviceClient.getTokenExpires());
    }

    @Test
    public void loginAwaitWithoutParameters() {
        Lsbl response = userServiceClient.loginAwait();

        assert LsBL.isResponse(response);
        String expectedToken = response.getResponse().getUserResponse().getUserLogin().getToken();
        long expectedExpires = response.getResponse().getUserResponse().getUserLogin().getExpires();
        assertEquals(expectedToken, serviceClient.getToken());
        assertEquals(expectedExpires, serviceClient.getTokenExpires());
    }

    @Test
    public void tokenRefreshWithoutToken() throws ExecutionException, InterruptedException {
        CompletableFuture<Lsbl> result = new CompletableFuture<>();
        userServiceClient.tokenRefresh(lsbl -> {
            result.complete(lsbl);
        });
        Lsbl response = result.get();
        assert LsBL.isNack(response);
    }

    @Test
    public void tokenRefreshWithToken() throws ExecutionException, InterruptedException {
        userServiceClient.loginAwait();
        String oldToken = serviceClient.getToken();

        CompletableFuture<Lsbl> result = new CompletableFuture<>();
        userServiceClient.tokenRefresh(lsbl -> {
            result.complete(lsbl);
        });
        Lsbl response = result.get();

        assert LsBL.isResponse(response);
        assertNotEquals(oldToken, serviceClient.getToken());
    }

    @Test
    public void tokenRefreshAwaitWithoutToken() {
        Lsbl response = userServiceClient.tokenRefreshAwait();
        assert LsBL.isNack(response);
    }

    @Test
    public void tokenRefreshAwaitWithToken() {
        userServiceClient.loginAwait();
        String oldToken = serviceClient.getToken();
        Lsbl response = userServiceClient.tokenRefreshAwait();

        assert LsBL.isResponse(response);
        assertNotEquals(oldToken, serviceClient.getToken());
    }

}
