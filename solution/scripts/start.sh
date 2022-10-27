#!/bin/sh

cd ../reference

docker-compose \
  -f ./docker-compose-infrastructure.yml \
  start

sleep 5

docker-compose \
  -f ./docker-compose-comfiguration.yml \
  start

sleep 5

docker-compose \
  -f ./docker-compose-service.yml \
  start

cd ..
