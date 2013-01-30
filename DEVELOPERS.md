# Notes for developers

## Building Cucumber-JVM

Cucumber-JVM is built with [Maven](http://maven.apache.org/). 

```
mvn clean install
```

## IDE Setup

### IntelliJ IDEA

```
File -> Open Project -> path/to/cucumber-jvm/pom.xml
```

Your `.feature` files must be in a folder that IDEA recognises as *source* or *test*. You must also tell IDEA to copy your `.feature` files to your output directory:

```
Preferences -> Compiler -> Resource Patterns -> Add `;?*.feature`
```

If you are writing step definitions in a scripting language you must also add the appropriate file extension for that language as well.

### Eclipse

Just load the root `pom.xml`

## Contributing/Hacking

To hack on Cucumber-JVM you need a JDK, Maven and Git to get the code. You also need to set your IDE/text editor to use:

* UTF-8 file encoding
* LF (UNIX) line endings
* No wildcard imports
* Curly brace on same line as block
* 4 Space indent (no tabs)
  * Java
  * XML
* 2 Space indent (no tabs)
  * Gherkin

Please do *not* add @author tags - this project embraces collective code ownership. If you want to know who wrote some
code, look in git. When you are done, send a [pull request](http://help.github.com/send-pull-requests/).
If we get a pull request where an entire file is changed because of insignificant whitespace changes we cannot see what
you have changed, and your contribution might get rejected.

### Running cross-platform Cucumber features

All Cucumber implementations (cucumber-ruby, cucumber-jvm, cucumber-js) share a common set of Cucumber features to 
ensure all implementations support the same basic features. To run these you need to clone the cucumber-tck repo into
your cucumber-jvm working copy:

    git submodule update --init

Now you can run the cross-platform Cucumber features:

    gem install bundler
    bundle install
    rake

## Troubleshooting

Below are some common problems you might encounter while hacking on Cucumber-JVM - and solutions.

### IntelliJ Idea fails to compile the generated I18n Java annotations

This can be solved by changing the Compiler settings: `Preferences -> Compiler -> Java Compiler`:

* *Use compiler:* `Javac in-process (Java6+ only)`
* *Additional command line parameters:* `-target 1.6 -source 1.6 -encoding UTF-8`

## Releasing

This is a reminder to the developers:

First, make sure you have the proper keys set up - in your `~/.m2/settings.xml` - for example:

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

Replace version numbers in:

* examples/java-helloworld/build.xml
* examples/java-helloworld/pom.xml
* examples/java-gradle/build.gradle
* README.md (this file)
* History.md

Run `git commit -am "Release X.Y.Z"`

Now release everything:

```
mvn release:clean
mvn --batch-mode -P release-sign-artifacts release:prepare -DautoVersionSubmodules=true -DdevelopmentVersion=1.1.3-SNAPSHOT
mvn -P release-sign-artifacts release:perform
```

Post release the API docs must be generated for each module and manually copied over to a working copy of the [cucumber.github.com](https://github.com/cucumber/cucumber.github.com) which must be a sibling of `cucumber-jvm` (this repo):

```
./doc/genapi.sh
```

After that's done, commit and push `cucumber.github.com`

## Code Coverage

Code coverage is collected mainly to identify code that can be deleted or needs to be tested better.
To generate a report, run:

```
COBERTURA_HOME=/some/where ./cobertura.sh
```

This technique to collect coverage for a multi-module Maven project is based on a
[blog post](http://thomassundberg.wordpress.com/2012/02/18/test-coverage-in-a-multi-module-maven-project/) by Thomas Sundberg.
