FROM maven:3.8-adoptopenjdk-8

## Add the wait script to the image
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.9.0/wait /wait
RUN chmod +x /wait

ADD start.sh /start.sh
RUN chmod +x /start.sh

COPY . /app/

WORKDIR /app/

CMD ["/bin/bash", "/app/start.sh"]
