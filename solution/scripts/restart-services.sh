#!/bin/sh

cd ../reference

docker-compose \
  -f ./docker-compose-service.yml \
  restart data history notification workflow gateway

cd ..
