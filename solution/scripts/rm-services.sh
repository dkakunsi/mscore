#!/bin/sh

cd ../reference

docker-compose \
    -f ./docker-compose.yml \
    -f ./docker-compose-logging.yml \
    -f ./docker-compose-data-service.yml \
    -f ./docker-compose-gateway-service.yml \
    -f ./docker-compose-notification-service.yml \
    -f ./docker-compose-workflow-service.yml \
    stop data history notification workflow gateway dashboard logger configuration

docker-compose \
    -f ./docker-compose.yml \
    -f ./docker-compose-logging.yml \
    -f ./docker-compose-data-service.yml \
    -f ./docker-compose-gateway-service.yml \
    -f ./docker-compose-notification-service.yml \
    -f ./docker-compose-workflow-service.yml \
    rm data history notification workflow gateway dashboard logger configuration

cd ..
