#!/bin/sh

cd ../reference

docker-compose \
  -f ./docker-compose-infrastructure.yml \
  -f ./docker-compose-configuration.yml \
  -f ./docker-compose-service.yml \
  stop

docker-compose \
  -f ./docker-compose-infrastructure.yml \
  -f ./docker-compose-configuration.yml \
  -f ./docker-compose-service.yml \
  rm

cd ..
