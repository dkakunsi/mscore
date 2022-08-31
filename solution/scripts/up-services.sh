#!/bin/sh

cd ../reference

# start the platform
docker-compose start

docker-compose up -d configuration

docker-compose \
    -f ./docker-compose.yml \
    -f ./docker-compose-logging.yml \
    -f ./docker-compose-data-service.yml \
    -f ./docker-compose-gateway-service.yml \
    -f ./docker-compose-notification-service.yml \
    -f ./docker-compose-workflow-service.yml \
    up -d data history notification workflow gateway logger dashboard configuration

docker-compose stop configuration
docker-compose rm configuration

cd ..
