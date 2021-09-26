build:
	mvn clean package -DskipTests
	docker-compose build java-maven hot-topic-analysis-service

start:
	docker run -d --rm --hostname dns.mageddo -v /var/run/docker.sock:/var/run/docker.sock -v /etc/resolv.conf:/etc/resolv.conf defreitas/dns-proxy-server
	docker-compose up -d zoo1 kafka1 spark-master spark-worker-1 namenode datanode resourcemanager nodemanager1 historyserver hbase

app:
	docker-compose up -d news-collection-service sentiment-analysis-service hot-topic-analysis-service sentimental-news-api
	docker-compose logs -f

clean:
	docker-compose stop news-collection-service sentiment-analysis-service hot-topic-analysis-service sentimental-news-api

clean-all:
	docker-compose down -v

restart-app:
	make clean
	make build
	make app

.PHONY: build start app clean clean-all restart-app
