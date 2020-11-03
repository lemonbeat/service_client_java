package com.lemonbeat.service_client;

import com.lemonbeat.lsbl.lsbl.Lsbl;
import com.lemonbeat.lsbl.lsbl_value_service.DeviceValueReported;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Starting point of our example application.
 */
public class Main {

    /**
     * Content of the settings.properties file if the ServiceClient was instanced with a properties file.
     */
    public static Properties settings;

    public static String YOUR_GW_SGTIN = "";
    public static String YOUR_DEVICE_SGTIN = "";
    public static String YOUR_DEVICE_UUID = "";


    public static void main(String[] args) throws Exception {

        // Load the configuration
        settings = new Properties();
        settings.load(new FileInputStream("settings.properties"));

        // Create an instance of the serviceClient using the settings.properties
        ServiceClient serviceClient = new ServiceClient("settings.properties");


        // Use the userService client to authenticate before sending calls to the backend.
        UserServiceClient userServiceClient = new UserServiceClient(serviceClient);

        // Example for a non blocking login request
        userServiceClient.login(settings.getProperty("BACKEND_USERNAME"), settings.getProperty("BACKEND_PASSWORD"), loginResponse -> {
            System.out.println("Login finished");
        });

        // Example for a blocking request that will return the response
        Lsbl loginResponse = userServiceClient.loginAwait(settings.getProperty("BACKEND_USERNAME"), settings.getProperty("BACKEND_PASSWORD"));

        // Example for a JWT refresh
        Lsbl tokenRefreshResponse = userServiceClient.tokenRefreshAwait();


        // #############################################################################################################
        // Event example
        // #############################################################################################################

        // Subscribing to events
        serviceClient.subscribe("EVENT.APP.VALUESERVICE.DEVICE_VALUE_REPORTED", event -> {
            DeviceValueReported deviceValueReportedEvent = event.getEvent().getValueEvent().getDeviceValueReported();
            System.out.println("Device SGTIN: " + deviceValueReportedEvent.getDeviceSgtin());
            // System.out.println("LsDL: "+ deviceValueReportedEvent.getLsdl());
        }, true);

        serviceClient.subscribe("EVENT.APP.#", event -> {
            System.out.println(event.getEvent().getName());
        }, true);


        // #############################################################################################################
        // Call examples: Uncomment the examples you wish to execute
        // #############################################################################################################

        // Get a list of all available gateways
        // Examples.example_gw_list_get(serviceClient);

        // Get a list of all devices known by the given gateway
        // Examples.example_gw_device_list_get(serviceClient, YOUR_GW_SGTIN);

        // Get the device_description_report of a device
        // Examples.example_device_description_get(serviceClient, YOUR_DEVICE_SGTIN);

        // Get Metadata by SGTIN
        // Examples.example_metadata_get(serviceClient, YOUR_DEVICE_SGTIN);

        // Get Metadata by UUID
        // Examples.example_metadata_get_by_uuid(serviceClient, YOUR_DEVICE_UUID);

        // Get values by SGTIN
        // Examples.example_value_get(serviceClient, YOUR_DEVICE_SGTIN);

        // Get values by UUID
        // Examples.example_value_get_by_uuid(serviceClient, YOUR_DEVICE_UUID);

        // Get the value_description_report of a device
        // Examples.example_value_description_get(serviceClient, YOUR_DEVICE_SGTIN);

        // Set values on a device
        // Examples.example_value_set(serviceClient, YOUR_DEVICE_SGTIN);

    }

}
