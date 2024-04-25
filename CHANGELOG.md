# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [7.1.0]

- Updated various dependencies

## [7.0.0]

- Updated dependencies to support Java 11 and higher.

## [6.1.0]

### Added
- Script to generate the partner certificate 'create-signing-request.sh'.

## [6.0.0]

### Changed

- Instead of separate files for the certificate and truststore, only one JKS file (which contains both) will be used. 

## [5.1.0]

### Changed

- The `settings.properties` supports disabling SSL for local development and testing.
- Tests can be run without a real OP instance by using mocked services and a local rabbit.

## [5.0.0]

## Breaking

- ServiceClient constructor will not exit when properties file is not found or connection fails, instead a runtime exception is thrown.
- Renamed package from `com.lemonbeat` to `com.lemonbeat.service_client`

### Added

- Support for MetricsCollector in the ServiceClient constructor.
- Getter for the current settings Properties object to ServiceClient.
- Implemented loginAwait without parameters in UserServiceClient.

### Changed

- Improved javadoc documentation.
- Updated the documentation with information on how to monitor the connections with a listener.
- Updated RabbitMQ Library to 5.9.0.

### Fixed

- Subscribe method will not cause an error when connection is closing while an AMQP ACK is attempted.
- Updated LsBL and LsDL libraries with versions that initialize the JAXB context only once.

## [4.0.0]

## Breaking

- Changed variable names in the settings.properties
    - KEYSTORE is now TRUSTSTORE
    - P12 is now CLIENT_P12

## Added

- Added ServiceClient.subscribe() method that allows using a durable queue.
- Added examples for getting and setting values by SGTIN and UUID.
- Added call which allows blocking calls that return the response.

## Changed

- ServiceClient.call() cleans up channels after the request is finished.  
    - Cleanup of channels when responses are received
    - Handling of timeouts when the backend does not reply  

## Fixed

- Improved reconnection behaviour of the ServiceClient.subscribe() method.