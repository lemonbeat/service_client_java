# Service Client Java

This project is an example on how to connect to the Lemonbeat Operations Platform using a Java based client.

It should help with the integration of your project into the [Lemonbeat End2End Device to Cloud IoT solution](https://www.lemonbeat.com/). 

The project uses [Gradle](https://gradle.org/) as its build tool, to see a list of all available task you can run:

```bash
./gradlew tasks
```

You can generate the Java documentation by running:

```bash
./gradlew javadoc
```

## Getting started

The broker uses AMQP which uses an encrypted connection. 
To connect with the java client you'll need a jks file and p12 file.

Both the p12 and jks require a enrollment/passcode, these will be different for each file.

Update the settings.properties with the path to these files and their enrollment/passcode.

The information for your `settings.properties` and the certificates will be provided by your Lemonbeat contact person.

You can create a new instance of the service client by passing the path to the settings.properties.

```java 
ServiceClient serviceClient = new ServiceClient("settings.properties");
```

## Making calls

```java 
ServiceClient serviceClient = new ServiceClient(connection);

Lsbl lsbl = LsBL.create(null, "SERVICE.TOPOSERVICE", 0);
Lsbl.Cmd cmd = new Lsbl.Cmd();
TopoCmd topoCmd = new TopoCmd();
GwListGetRequest gwListGetRequest = new GwListGetRequest();
topoCmd.setGwListGet(gwListGetRequest);
cmd.setTopoCmd(topoCmd);
lsbl.setCmd(cmd);

serviceClient.call(request, new ServiceClient.ResponseCallback() {
    @Override
    public void onResponse(Lsbl response) {
        // Do something with the response     
    }
});
```

You can also use lambdas for a shorter call:

```java 
serviceClient.call(request,  response -> {
    // Do something with the response
});
```

## Making blocking calls

You can use the callAwait method to make blocking calls that will return the response directly.

```java 
Lsbl response = serviceClient.callAwait(request)
// Do something with the response
```

## Subscribing to events

You can subscribe to events by providing the name of the event and passing a callback function.

```java
serviceClient.subscribe("EVENT.APP.VALUESERVICE.DEVICE_VALUE_REPORTED", event -> {
    System.out.printf(LsBL.write(event));
});
```

This will create a temporary queue where the events are received until your consumer disconnects.

To create a permanent queue that will store the messages when no consumer is connected use this:

```java
serviceClient.subscribe("EVENT.APP.VALUESERVICE.DEVICE_VALUE_REPORTED", event -> {
    System.out.printf(LsBL.write(event));
}, true);
```

## Connection management and monitoring

The official RabbitMQ Java library recovers connections and channels when a connection to the broker is lost.
To listen to such events as reconnections one could implement a listener to receive the close of the connection.

```java
serviceClient.getConnection().addShutdownListener(cause -> {
    // Act on the connection shutdown
});
```

Depending on your application it might be helpful to monitor the rabbit metrics using a MetricsCollector.

An example implementation can be found in `test/java/com/lemonbeat/ExampleMetricsCollector`.

The metrics collector can be referenced in the Service Client constructor.

```java
ExampleMetricsCollector metricsCollector = new ExampleMetricsCollector();
ServiceClient serviceClient = new ServiceClient("settings.properties", metricsCollector);
```

Further information about the different metric collectors can be found [here](https://www.rabbitmq.com/api-guide.html#metrics).