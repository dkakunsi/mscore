#!/bin/sh

cd ../reference

docker-compose \
    -f ./docker-compose.yml \
    -f ./docker-compose-logging.yml \
    -f ./docker-compose-data-service.yml \
    -f ./docker-compose-gateway-service.yml \
    -f ./docker-compose-workflow-service.yml \
    stop

docker-compose \
    -f ./docker-compose.yml \
    -f ./docker-compose-logging.yml \
    -f ./docker-compose-data-service.yml \
    -f ./docker-compose-gateway-service.yml \
    -f ./docker-compose-notification-service.yml \
    -f ./docker-compose-workflow-service.yml \
    rm

cd ..
