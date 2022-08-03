SHELL := /bin/bash
GREEN  := $(shell tput -Txterm setaf 2)
WHITE  := $(shell tput -Txterm setaf 7)
YELLOW := $(shell tput -Txterm setaf 3)
RESET  := $(shell tput -Txterm sgr0)

# Add the following 'help' target to your Makefile
# And add help text after each target name starting with '\#\#'
# A category can be added with @category
HELP_FUN = \
					 %help; \
					 while(<>) { push @{$$help{$$2 // 'options'}}, [$$1, $$3] if /^([a-zA-Z\-\_]+)\s*:.*\#\#(?:@([a-zA-Z\-]+))?\s(.*)$$/ }; \
					 print "usage: make [target]\n\n"; \
					 for (sort keys %help) { \
					 print "${WHITE}$$_:${RESET}\n"; \
					 for (@{$$help{$$_}}) { \
					 $$sep = " " x (32 - length $$_->[0]); \
					 print "  ${YELLOW}$$_->[0]${RESET}$$sep${GREEN}$$_->[1]${RESET}\n"; \
					 }; \
					 print "\n"; }

.DEFAULT_GOAL := help
ROOT_DIR=$(realpath $(shell pwd))

build: ## Build a release within docker
	docker run --rm \
	-v $(ROOT_DIR):/opt/service_client \
	-w /opt/service_client \
	openjdk:8-jdk-buster \
	/bin/bash -c "\
	./gradlew build && \
	./gradlew test && \
	chown $(shell id -u):$(shell id -g) /opt/service_client/* -R"

docker:
	docker run --rm \
	--net=host \
	-v $(ROOT_DIR):/opt/service_client \
	-w /opt/service_client -it \
	openjdk:8-jdk-buster \
	/bin/bash 
