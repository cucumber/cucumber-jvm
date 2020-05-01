SHELL := /usr/bin/env bash

default:
	mvn clean install
.PHONY: default

VERSION = $(shell mvn org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -q -DforceStdout 2> /dev/null)
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

update-dependency-versions:
	mvn versions:force-releases
	mvn versions:update-properties -DallowMajorUpdates=false -Dmaven.version.rules="file://`pwd`/.m2/maven-version-rules.xml"
.PHONY: update-dependency-versions

update-major-dependency-versions:
	mvn versions:force-releases
	mvn versions:update-properties -DallowMajorUpdates=true -Dmaven.version.rules="file://`pwd`/.m2/maven-version-rules.xml"
.PHONY: update-major-dependency-versions

update-changelog:
	cat CHANGELOG.md | ./scripts/update-changelog.sh $(NEW_VERSION) > CHANGELOG.md.tmp
	mv CHANGELOG.md.tmp CHANGELOG.md
.PHONY: update-changelog

.commit-and-push-changelog:
	git commit -am "Update CHANGELOG for v$(NEW_VERSION)"
	git push
.PHONY: .commit-and-push-changelog

release: default update-changelog .commit-and-push-changelog
	mvn --batch-mode release:clean release:prepare -DautoVersionSubmodules=true -Darguments="-DskipTests=true -DskipITs=true -Darchetype.test.skip=true"
	git checkout "v$(NEW_VERSION)"
	mvn deploy -P-examples -Psign-source-javadoc -DskipTests=true -DskipITs=true -Darchetype.test.skip=true
	git checkout $(CURRENT_BRANCH)
.PHONY: release
