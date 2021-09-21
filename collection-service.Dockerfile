FROM maven:3.8-adoptopenjdk-16 AS builder

ENV APPLICATION_JAR=collection-service-0.0.1.jar

COPY ./pom.xml /
COPY ./collection-service /app/

WORKDIR /app/
RUN mvn package

RUN ls /app/target

## Add the wait script to the image
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.9.0/wait /wait
RUN chmod +x /wait

RUN chmod +x /app/start.sh

CMD ["/bin/bash", "/app/start.sh"]