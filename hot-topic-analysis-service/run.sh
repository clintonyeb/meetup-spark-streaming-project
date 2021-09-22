#!/bin/bash

chmod u+x "${SECRET_FILE}"
source "${SECRET_FILE}"
export $(cut -d= -f1 "${SECRET_FILE}")

cp "/app/${SERVICE_NAME}/target/${SERVICE_NAME}-${APP_VERSION}.jar" "${SPARK_APPLICATION_JAR_LOCATION}"

sh -c "/wait && /submit.sh"