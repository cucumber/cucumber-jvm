Releasing
=========

The deployment process of `cucumber-jvm` is based on 
[Deploying to OSSRH with Apache Maven](http://central.sonatype.org/pages/apache-maven.html#deploying-to-ossrh-with-apache-maven-introduction).

## Check [![Build Status](https://travis-ci.org/cucumber/cucumber-jvm.svg?branch=main)](https://travis-ci.org/cucumber/cucumber-jvm) ##

Is the build passing?

```
git checkout main
```

Also check if you can upgrade any dependencies:

```
make update-dependency-versions
```

## Decide what the next version should be ##

This depends on what's changed (see `CHANGELOG.md`):

* Bump `MAJOR` if:
  * There are `Changed` or `Removed` entries
  * A cucumber library dependency upgrade was major
* Bump `MINOR` if:
  * There are `Added` entries
* Bump `PATCH` if:
  * There are `Fixed` or `Deprecated` entries

Display future version by running:

```
make version
```

Check if branch name and version are as expected. To change version run:

```
mvn versions:set -DnewVersion=X.Y.Z-SNAPSHOT
```

## Secrets ##

Secrets are required to make releases. Members of the core team can install
keybase and join the `cucumberbdd` team to access these secrets.

During the release process, secrets are fetched from keybase and used to sign
and upload the maven artifacts.

## Make the release ##

Check if branch name and version are as expected:

```
make version
```

Do the release:

```
make release
``` 

## Last bits ##

Update the cucumber-jvm version in the documentation project:

* https://github.com/cucumber/docs.cucumber.io

The cucumber-jvm version for the docs is specified in the docs [versions.yaml](https://github.com/cucumber/docs.cucumber.io/blob/master/data/versions.yaml)

All done! Hurray!
