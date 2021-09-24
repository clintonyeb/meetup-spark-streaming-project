build:
	docker-compose build java-maven hot-topic-analysis-service

start:
	docker-compose up -d zoo1 kafka1 spark-master spark-worker-1

app:
	make clean
	docker-compose up -d collection-service sentiment-analysis-service hot-topic-analysis-service
	docker-compose logs -f

clean:
	docker-compose stop collection-service sentiment-analysis-service hot-topic-analysis-service

clean-all:
	docker-compose down -v

restart-app:
	make clean
	make build
	make app

.PHONY: build start app clean clean-all restart-app
