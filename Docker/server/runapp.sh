#!/bin/bash
set -e

APP=app.jar
JAVA_PARAMS="-XX:+UseContainerSupport"

echo " --- RUNNING $(basename "$0") $(date -u "+%Y-%m-%d %H:%M:%S Z") --- "
set -x

# If AZURE_KEYVAULT_ENABLED equals "true"
# The properties with path $AZURE_KEYVAULT_PATH will be written to $AZURE_KEYVAULT_OUTPUTPATH
# The path is stripped to the output. eg /dev/myapp/db.password=1234 is added to the file as db.password=1234.
if [ "$AZURE_KEYVAULT_ENABLED" = "true" ]; then
    touch $AZURE_KEYVAULT_OUTPUTPATH
    if python3 GetSecretsFromKeyVault.py $AZURE_KEYVAULT_PATH $AZURE_KEYVAULT_OUTPUTPATH; then
        echo 'GetSecretsFromKeyVault.py exited successfully!'
    else
        echo 'GetSecretsFromKeyVault.py exited with error (non null exit code)'
        exit 1
    fi
fi

java $JAVA_PARAMS $JAVA_PARAMS_OVERRIDE -jar $APP
