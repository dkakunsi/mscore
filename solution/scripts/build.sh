#!/bin/sh

cd ../reference

docker-compose \
    -f ./docker-compose.yml \
    -f ./docker-compose-logging.yml \
    -f ./docker-compose-data-service.yml \
    -f ./docker-compose-gateway-service.yml \
    -f ./docker-compose-notification-service.yml \
    -f ./docker-compose-workflow-service.yml \
    stop data gateway history notification workflow logger configuration

docker-compose \
    -f ./docker-compose.yml \
    -f ./docker-compose-logging.yml \
    -f ./docker-compose-data-service.yml \
    -f ./docker-compose-gateway-service.yml \
    -f ./docker-compose-notification-service.yml \
    -f ./docker-compose-workflow-service.yml \
    rm data history gateway notification workflow logger configuration

# docker-compose stop

cd ..

build () {
    printf "\n=== BUILDING $SERVICE === \n"
    cd ./$SERVICE
    docker build -t $REPO/$SOLUTION-$SERVICE:$VERSION .
    cd ..
}

REPO=devit16
SOLUTION=ref
SERVICE=nothing
VERSION=latest

# build data-service
SERVICE=data
build

# build workflow-service
SERVICE=workflow
build

# build gateway-service
SERVICE=gateway
build

# build notification-service
SERVICE=notification
build

# build history
SERVICE=history
build

# build logger
SERVICE=logger
build

# build configuration
SERVICE=configuration
cd $SERVICE
./load.sh > ./init
cd ..
build
cd $SERVICE
rm init
cd ..

cd ..
