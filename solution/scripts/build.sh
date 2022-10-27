#!/bin/sh

cd ../reference

docker-compose \
  -f ./docker-compose-configuration.yml \
  -f ./docker-compose-service.yml \
  stop

docker-compose \
  -f ./docker-compose-configuration.yml \
  -f ./docker-compose-service.yml \
  rm

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
