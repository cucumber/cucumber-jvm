Releasing
=========

The deployment process of `cucumber-jvm` is based on 
[Deploying to OSSRH with Apache Maven](http://central.sonatype.org/pages/apache-maven.html#deploying-to-ossrh-with-apache-maven-introduction).

## Check [![Build Status](https://travis-ci.org/cucumber/cucumber-jvm.svg?branch=master)](https://travis-ci.org/cucumber/cucumber-jvm) ##

Is the build passing?

```
git checkout master
```

Also check if you can upgrade any dependencies:

```
make update-dependencies
```

## Prepare for release ##

Update the version numbers in the changelog by running (replace X.Y.Z below with the next release number): 

```
make update-changelog NEW_VERSION=X.Y.Z
```

Then run (replace X.Y.Z below with the next release number): 

```
git commit -am "Prepare for release X.Y.Z"
``` 

## Make the release ##

Now release everything:

```
mvn release:clean release:prepare -DautoVersionSubmodules=true -Darguments="-DskipTests=true -DskipITs=true"
git checkout vX.Y.Z
mvn clean deploy -P-examples -Psign-source-javadoc -DskipTests=true -DskipITs=true
```

## Last bits ##

Wait for the release to show up on maven central. Then update the dependency in example projects:

* https://github.com/cucumber/cucumber-java-skeleton

Update the cucumber-jvm version in the documentation project:

* https://github.com/cucumber/docs.cucumber.io

The cucumber-jvm version for the docs is specified in the docs [versions.yaml](https://github.com/cucumber/docs.cucumber.io/blob/master/data/versions.yaml)

All done! Hurray!


# GPG Keys #

To make a release you must have the `devs@cucumber.io` GPG private key imported in gpg2.

```
gpg --import devs-cucumber.io.key
```

Additionally upload privileges to the Sonatype repositories are required. See the 
[OSSRH Guide](http://central.sonatype.org/pages/ossrh-guide.html) for instructions. Then an 
administrator will have to grant you access to the cucumber repository.

Finally both your OSSRH credentials and private key must be setup in your `~/.m2/settings.xml` - 
for example:

```
<settings>
    <servers>
        <server>
            <id>ossrh</id>
            <username>sonatype-user-name</username>
            <password>sonatype-password</password>
        </server>
    </servers>
    <profiles>
        <profile>
            <id>ossrh</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <gpg.executable>gpg2</gpg.executable>
                <gpg.useagent>true</gpg.useagent>
            </properties>
        </profile>
        <profile>
            <id>sign-with-cucumber-key</id>
            <properties>
                <gpg.keyname>dev-cucumber.io-key-id</gpg.keyname>
            </properties>
        </profile>
    </profiles>
</settings>
```
