#!/bin/bash

#chmod u+x "${SECRET_FILE}"
#source "${SECRET_FILE}"
#export $(cut -d= -f1 "${SECRET_FILE}")

export SPARK_MASTER_URL=spark://${SPARK_MASTER_NAME}:${SPARK_MASTER_PORT}
export SPARK_HOME=/spark

cp "/app/${SERVICE_NAME}/target/${SERVICE_NAME}-${APP_VERSION}.jar" "${SPARK_APPLICATION_JAR_LOCATION}"

sh -c /wait
exec "$@"
