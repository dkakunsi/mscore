# README

Common repo is library containing all stuff used in services.

### Project Structure

This library consists of:

1. `api`: mscore core api.
2. `component`: implementation of specific library.

Parent pom can be found in [common repository](https://bitbucket.org/devit/common).

### How do I get set up?

To set up all common projects, you need to clone this repository into your local repository and run `mvn clean install`.

All resulting library will be installed into maven local repository and available for building of all services.

#### Configuration

All configuration will be available in each service under `src/main/resources/` for example app.config.
