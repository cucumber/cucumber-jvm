SHELL := /usr/bin/env bash

default:
	mvn clean install
.PHONY: default

VERSION = $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2> /dev/null)
NEW_VERSION = $(subst -SNAPSHOT,,$(VERSION))
CURRENT_BRANCH = $(shell git rev-parse --abbrev-ref HEAD)

clean:
	mvn clean release:clean
.PHONY: clean

version:
	@echo ""
	@echo "The next version of Cucumber-JVM will be $(NEW_VERSION) and released from '$(CURRENT_BRANCH)'"
	@echo ""
.PHONY: version

update-cck:
ifndef CCK_VERSION
	@echo -e "\033[0;31mCCK_VERSION is not defined. Can't update CCK :-(\033[0m"
	exit 1
endif
	git clone --branch v$$CCK_VERSION git@github.com:cucumber/compatibility-kit.git target/compatibility-kit
	cd target/compatibility-kit/devkit && npm install && npm run generate-ndjson
	rm -rf compatibility/src/test/resources/features/*
	cp -r target/compatibility-kit/devkit/samples/* compatibility/src/test/resources/features
	rm -rf target/compatibility-kit
.PHONY: update-cck
