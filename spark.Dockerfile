FROM bde2020/spark-submit:2.4.5-hadoop2.7

ENV SPARK_MASTER_NAME spark-master
ENV SPARK_MASTER_PORT 7077
ENV SPARK_APPLICATION_MAIN_CLASS com.clinton.Application

COPY hot-topic-analysis/run.sh /

RUN apk add --no-cache openjdk8 maven\
      && chmod +x /run.sh \
      && mkdir -p /app

# Copy the POM-file first, for separate dependency resolving and downloading
COPY pom.xml /app
RUN cd /app && mvn dependency:resolve

RUN cd /app && mvn verify

# Copy the source code and build the application
COPY . /app
RUN cd /app && mvn clean package -DskipTests

CMD ["/bin/bash", "/run.sh"]
