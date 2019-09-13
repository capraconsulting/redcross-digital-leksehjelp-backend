#!/usr/bin/env bash
mvn clean install && cd Docker/ && ./test-docker.sh
