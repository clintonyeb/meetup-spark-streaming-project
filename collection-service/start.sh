#!/bin/bash

chmod u+x "${SECRET_FILE}"
source "${SECRET_FILE}"
export $(cut -d= -f1 "${SECRET_FILE}")

sh -c "/wait && java -jar /app/target/collection-service-0.0.1.jar"
