#!/bin/bash

docker-compose down
set -e
cp -p "../target/$(ls -t ../target/*.jar | grep -v /orig | head -1)" server/app.jar
docker-compose build
docker-compose up

