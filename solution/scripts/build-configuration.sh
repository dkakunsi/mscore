#!/bin/sh

cd ../reference

docker-compose \
  -f ./docker-compose-configuration.yml \
  stop

docker-compose \
  -f ./docker-compose-configuration.yml \
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
