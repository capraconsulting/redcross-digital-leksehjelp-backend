#!/bin/bash
set -e

APP=app.jar
JAVA_PARAMS="-XX:+UseContainerSupport"

echo " --- RUNNING $(basename "$0") $(date -u "+%Y-%m-%d %H:%M:%S Z") --- "
set -x

# If AWS_PARAMETER_STORE_ENABLED equals "true"
# The properties with path $AWS_PARAMETER_STORE_PATH will be written to $AWS_PARAMETER_STORE_OUTPUTPATH
# The path is stripped to the output. eg /dev/myapp/db.password=1234 is added to the file as db.password=1234.
if [ "$AWS_PARAMETER_STORE_ENABLED" = "true" ]; then
    mkdir -p "$(dirname "$AWS_PARAMETER_STORE_OUTPUTPATH")"
    if python GetPropertiesFromParameterStore.py $AWS_PARAMETER_STORE_PATH $AWS_PARAMETER_STORE_OUTPUTPATH; then
        echo 'GetPropertiesFromParameterStore.py exited successfully!'
    else
        echo 'GetPropertiesFromParameterStore.py exited with error (non null exit code)'
        exit 1
    fi
fi

java $JAVA_PARAMS $JAVA_PARAMS_OVERRIDE -jar $APP
