FROM bde2020/spark-submit:3.1.1-hadoop3.2

COPY --from=java-maven:latest /wait /
COPY --from=java-maven:latest /app /app
COPY hot-topic-analysis-service/run.sh /

RUN chmod u+x /run.sh

ENTRYPOINT ["/run.sh"]
CMD ["/bin/bash", "/submit.sh"]
