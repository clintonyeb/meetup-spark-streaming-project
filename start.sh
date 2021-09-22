#!/bin/bash

chmod u+x "${SECRET_FILE}"
source "${SECRET_FILE}"
export $(cut -d= -f1 "${SECRET_FILE}")

sh -c "/wait && java -jar /app/${SERVICE_NAME}/target/${SERVICE_NAME}-${APP_VERSION}.jar"
