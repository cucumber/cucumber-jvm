Releasing
=========

The deployment process of `cucumber-jvm` is based on 
[Deploying to OSSRH with Apache Maven](http://central.sonatype.org/pages/apache-maven.html#deploying-to-ossrh-with-apache-maven-introduction).

## Check [![Build Status](https://github.com/cucumber/cucumber-jvm/workflows/Cucumber%20CI/badge.svg)](https://github.com/cucumber/cucumber-jvm/actions) ##

Is the build passing?

```
git checkout main
```

Also check if you can upgrade any dependencies:

```
make update-dependency-versions
```

## Decide what the next version should be ##

Versions follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html). To sum it up, it depends on what's changed (see `CHANGELOG.md`). Given a version number MAJOR.MINOR.PATCH:

* Bump `MAJOR` when you make incompatible API changes:
  * There are `Removed` entries, or `Changed` entries breaking compatibility
  * A cucumber library dependency upgrade was major
* Bump `MINOR` when you add functionality in a backwards compatible manner:
  * There are `Added` entries, `Changed` entries preserving compatibility, or
  `Deprecated` entries
* Bump `PATCH` when you make backwards compatible bug fixes:
  * There are `Fixed` entries

Display future version by running:

```
make version
```

Check if branch name and version are as expected. To change version run:

```
mvn versions:set -DnewVersion=X.Y.Z-SNAPSHOT
```

## Make the release ##

Only people with permission to push to `release/*` branches can make releases.

1. Check if branch name and version are as expected:

```
make version
```

2. Do the release:

```
make release
``` 

3. Wait until the `release-*` workflows have passed for the new release branch:
  * [![Release maven package](https://github.com/cucumber/cucumber-jvm/actions/workflows/release-java.yml/badge.svg)](https://github.com/cucumber/cucumber-jvm/actions/workflows/release-java.yml)
  * [![Release on GitHub](https://github.com/cucumber/cucumber-jvm/actions/workflows/release-github.yml/badge.svg)](https://github.com/cucumber/cucumber-jvm/actions/workflows/release-github.yml)

## Last bits ##

Update the cucumber-jvm version in the documentation project:

* https://github.com/cucumber/docs.cucumber.io

The cucumber-jvm version for the docs is specified in the docs [versions.yaml](https://github.com/cucumber/docs.cucumber.io/blob/master/data/versions.yaml)

All done! Hurray!

[changelog]: https://github.com/cucumber/changelog#installation
