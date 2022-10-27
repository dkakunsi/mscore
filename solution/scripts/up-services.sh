#!/bin/sh

cd ../reference

docker-compose \
  -f ./docker-compose-infrastructure.yml \
  start

sleep 5

docker-compose \
  -f ./docker-compose-configuration.yml \
  start

sleep 5

docker-compose \
  -f ./docker-compose-service.yml \
  up -d

docker-compose \
  -f ./docker-compose-configuration.yml \
  stop configuration

docker-compose \
  -f ./docker-compose-configuration.yml \
  rm configuration

cd ..
