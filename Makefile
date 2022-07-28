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

update-dependency-versions:
	mvn versions:force-releases
	mvn versions:update-properties -DallowMajorUpdates=false -Dmaven.version.rules="file://`pwd`/.versions/rules.xml"
.PHONY: update-dependency-versions

update-major-dependency-versions:
	mvn versions:force-releases
	mvn versions:update-properties -DallowMajorUpdates=true -Dmaven.version.rules="file://`pwd`/.versions/rules.xml"
.PHONY: update-major-dependency-versions

update-changelog:
	cat CHANGELOG.md | ./scripts/update-changelog.sh $(NEW_VERSION) > CHANGELOG.md.tmp
	mv CHANGELOG.md.tmp CHANGELOG.md
.PHONY: update-changelog

.commit-and-push-changelog:
	git commit -am "Update CHANGELOG for v$(NEW_VERSION)"
	git push
.PHONY: .commit-and-push-changelog

.configure-cukebot-in-docker:
	[ -f '/home/cukebot/configure' ] && /home/cukebot/configure
.PHONY: .configure-cukebot-in-docker

.release-in-docker: .configure-cukebot-in-docker default update-changelog .commit-and-push-changelog
	mvn --batch-mode release:clean release:prepare -DautoVersionSubmodules=true -Darguments="-DskipTests=true -DskipITs=true -Darchetype.test.skip=true"
	git checkout "v$(NEW_VERSION)"
	mvn deploy -P-build-in-ci -Psign-source-javadoc -DskipTests=true -DskipITs=true -Darchetype.test.skip=true
	git checkout $(CURRENT_BRANCH)
	git fetch
.PHONY: .release-in-docker

release:
	[ -d '../secrets' ]  || git clone keybase://team/cucumberbdd/secrets ../secrets
	git -C ../secrets pull
	../secrets/update_permissions
	docker run \
	  --volume "${shell pwd}":/app \
 	  --volume "${shell pwd}/../secrets/configure":/home/cukebot/configure \
	  --volume "${shell pwd}/../secrets/codesigning.key":/home/cukebot/codesigning.key \
	  --volume "${shell pwd}/../secrets/gpg-with-passphrase":/home/cukebot/gpg-with-passphrase \
	  --volume "${shell pwd}/../secrets/.ssh":/home/cukebot/.ssh \
	  --volume "${HOME}/.m2/repository":/home/cukebot/.m2/repository \
	  --env-file "${shell pwd}/../secrets/secrets.list" \
	  --user 1000 \
	  --rm \
	  -it cucumber/cucumber-build:0.1.0@sha256:2ce049493dfadad62b78594e6728d1f85ccc5a2441b5a8b3f7a106a7bba39ec1 \
	  make .release-in-docker
.PHONY: release
