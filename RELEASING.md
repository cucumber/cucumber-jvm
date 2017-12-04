Releasing
=========

Run `mvn --version` to ensure your java version is 8 or higher.

Upload privileges to the Sonatype staging repository are required.

First, make sure everything builds. Including Android.

Then, see if you can upgrade any dependencies:

```
mvn versions:display-dependency-updates
```

This is a reminder to the developers:

Then, make sure you have the proper keys set up - in your `~/.m2/settings.xml` - for example:

```
<settings>
  <servers>
    <server>
      <id>cukes.info</id>
      <username>yourcukesinfouser</username>
      <privateKey>fullkeypath</privateKey>
    </server>
    <!-- See https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide -->
    <server>
      <id>sonatype-nexus-snapshots</id>
      <username>yoursonatypeuser</username>
      <password>TOPSECRET</password>
    </server>
    <server>
      <id>sonatype-nexus-staging</id>
      <username>yoursonatypeuser</username>
      <password>TOPSECRET</password>
    </server>
  </servers>
</settings>
```

Make sure you can generate Javadocs for all modules, or else the
release will fail:

```
mvn javadoc:javadoc
```

## Update versions ##

Replace version numbers in:

* `examples/java-gradle/build.gradle`
* `examples/android/android-studio/Cukeulator/app/build.gradle`
* `CHANGELOG.md`

Then run (replace X.Y.Z below with the next release number): 

```
git commit -am "Prepare for release X.Y.Z"
```

## Make the release ##

Now release everything:

```
mvn release:clean
mvn --batch-mode -P release-sign-artifacts release:prepare -DautoVersionSubmodules=true -DdevelopmentVersion=X.Y.Z-SNAPSHOT
mvn -P release-sign-artifacts release:perform
```

Update the pom.xml file for the examples/android modules using `mvn versions:set` (which are not automatically updated 
by the release process), commit and push.

Then go into [Nexus](https://oss.sonatype.org/) and inspect, close and release the staging repository.

## Publish the Javadoc ##

Post release the API docs must be generated for each module and manually copied over to a working copy of the 
[api.cucumber.io](https://github.com/cucumber/api.cucumber.io) which must be a sibling of `cucumber-jvm` (this repo):

```
git checkout vX.Y.Z
./doc/genapi.sh X.Y.Z
cd ../api.cucumber.io/cucumber-jvm
git add X.Y.Z 
git commit -m "Add the docs for Cucumber-JVM vX.Y.Z."
```

After that's done, push `api.cucumber.io`

## Last bits ##

Finally update the dependency in example projects:

* https://github.com/cucumber/cucumber-java-skeleton

All done! Hurray!