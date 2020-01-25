SHELL := /usr/bin/env bash

default:
	mvn install
.PHONY: default

clean:
	mvn clean release:clean
.PHONY: clean

update-dependency-versions:
	mvn versions:force-releases
	mvn versions:update-properties -DallowMajorUpdates=false -Dmaven.version.rules="file://`pwd`/.m2/maven-version-rules.xml"
.PHONY: update-dependency-versions

update-major-dependency-versions:
	mvn versions:force-releases
	mvn versions:update-properties -DallowMajorUpdates=true -Dmaven.version.rules="file://`pwd`/.m2/maven-version-rules.xml"
.PHONY: update-major-dependency-versions

update-changelog:
ifdef NEW_VERSION
	cat CHANGELOG.md | ./scripts/update-changelog.sh $(NEW_VERSION) > CHANGELOG.md.tmp
	mv CHANGELOG.md.tmp CHANGELOG.md
else
	@echo -e "\033[0;31mNEW_VERSION is not defined. Can't update version :-(\033[0m"
	exit 1
endif
.PHONY: update-changelog

.commit-and-push-changelog:
ifdef NEW_VERSION
	git commit -am "Update CHANGELOG for v$(NEW_VERSION)"
	git push
else
	@echo -e "\033[0;31mNEW_VERSION is not defined. Can't update version :-(\033[0m"
	exit 1
endif
.PHONY: .commit-and-push-changelog

release: update-changelog .commit-and-push-changelog
ifdef NEW_VERSION
	mvn release:clean release:prepare -DautoVersionSubmodules=true -Darguments="-DskipTests=true -DskipITs=true -Darchetype.test.skip=true"
	mvn release:perform -P-examples -Psign-source-javadoc
else
	@echo -e "\033[0;31mNEW_VERSION is not defined. Can't release. :-(\033[0m"
	exit 1
endif
.PHONY: release
