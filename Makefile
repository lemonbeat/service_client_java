SHELL := /bin/bash
.DEFAULT_GOAL := help
ROOT_DIR=$(realpath $(shell pwd))

# https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
help: ## Print this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.PHONY: build
build: ## Build a release within docker
	docker run --rm \
	--net=host \
	-v $(ROOT_DIR):/opt/service_client \
	-w /opt/service_client \
	openjdk:11-jdk-buster \
	/bin/bash -c "\
	./gradlew build && \
	./gradlew test && \
	chown $(shell id -u):$(shell id -g) /opt/service_client/* -R"

.PHONY: docker
docker: ## Start an interactive docker container and attach to it
	docker run --rm \
	--net=host \
	-v $(ROOT_DIR):/opt/service_client \
	-w /opt/service_client -it \
	openjdk:11-jdk-buster \
	/bin/bash 
