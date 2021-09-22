DOCKER_NETWORK=meetup-spark-spark-stream-project_default

build:
	docker-compose build java-maven

start:
	docker-compose up -d zoo1 kafka1

app:
	make clean
	docker-compose up -d collection-service sentiment-analysis-service
	docker-compose logs -f

clean:
	docker-compose stop collection-service sentiment-analysis-service

clean-all:
	docker-compose down -v

restart-app:
	make clean
	makle build
	make app

.PHONY: build start app clean clean-all restart-app
