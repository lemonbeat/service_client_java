# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.0.0]

- Changed variable names in the settings.properties
    - KEYSTORE is now TRUSTSTORE
    - P12 is now CLIENT_P12
- ServiceClient.call() cleans up channels after the request is finished.  
    - Cleanup of channels when responses are received
    - Handling of timeouts when the backend does not reply  
- Improved reconnection behaviour of the ServiceClient.subscribe() method.
- Added ServiceClient.subscribe() method that allows using a durable queue.
- Added examples for getting and setting values by SGTIN and UUID.
- Added call which allows blocking calls that return the response.