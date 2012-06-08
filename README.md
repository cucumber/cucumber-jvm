[![Build Status](https://secure.travis-ci.org/cucumber/cucumber-jvm.png)](http://travis-ci.org/cucumber/cucumber-jvm)

Cucumber-JVM is a pure Java implementation of Cucumber that supports the following programming languages:

* Clojure
* Groovy
* Ioke
* Java
* JavaScript (Rhino interpreter)
* Python (Jython interpreter)
* Ruby (JRuby interpreter)
* Scala

Cucumber-JVM provides the following mechanisms for running Cucumber Features:

* Command Line
* JUnit (via IDE, Maven, Ant or anything that knows how to run JUnit)

Cucumber-JVM also integrates with the following Dependency Injection containers:

* Guice
* PicoContainer
* Spring
* CDI/Weld
* OpenEJB

## Hello World

Check out the simple [Hello World](https://github.com/cucumber/cucumber-jvm/tree/master/examples/java-helloworld) example.

## Downloading / Installation

Releases are published in [Maven Central](http://search.maven.org/)

### Getting jars

Jar files can be browsed and downloaded from [Maven Central] or https://oss.sonatype.org/content/repositories/releases/info/cukes/ 
(New releases will show up here immediately, while it takes a couple of hours to sync to Maven Central).

### Using Maven with JUnit

If you want to write simple Cucumber tests with JUnit add the following dependencies 
in your [POM](http://maven.apache.org/pom.html):

```xml
<dependency>
    <groupId>info.cukes</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>1.0.9</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>info.cukes</groupId>
    <artifactId>cucumber-junit</artifactId>
    <version>1.0.9</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.10</version>
    <scope>test</scope>
</dependency>
```

There are more jars available - add the ones you need. (TODO: A guide on how to pick the right jars needs to be written)

### Using Ivy

Add a [dependency](http://ant.apache.org/ivy/history/latest-milestone/ivyfile/dependency.html) in your [ivy.xml](http://ant.apache.org/ivy/history/latest-milestone/ivyfile.html):

```xml
    <dependency org="info.cukes" name="cucumber-core" rev="1.0.9"/>
```

Since the artifacts are released to Maven Central, the default Ivy configuration should pull them down automatically.
Alternatively you can define your own resolver:

```xml
<ibiblio name="sonatype"
    m2compatible="true"
    usepoms="true"
    pattern="[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]"
    root="https://oss.sonatype.org/content/repositories/releases"/>
```

## Documentation

There isn't any online documentation yet. Check out the examples, read the code and ask specific questions on the Cucumber mailing list.

### API Docs

* http://cukes.info/cucumber/jvm/api/1.0.9

## Examples

You will find an example in Git under `examples`. Before you can run any of them you need to build cucumber-jvm itself (see below).

You should now be able to run any of the examples simply by `cd`ing into a directory and running `mvn clean integration-test`.

## Building Cucumber-JVM

Cucumber-JVM is built with [Maven](http://maven.apache.org/). 

    mvn clean install

This will generate some code (i18n step definition code for various backends), and you have to build from the command
line once before you'll be able to compile it in an IDE.

## IDE Setup

### IntelliJ IDEA

    File -> Open Project -> path/to/cucumber-jvm/pom.xml

Your `.feature` files must be in a folder that IDEA recognises as *source* or *test*. You must also tell IDEA to copy your `.feature` files to your output directory:

    Preferences -> Compiler -> Resource Patterns -> Add `;?*.feature`

If you are writing step definitions in a scripting language you must also add the appropriate file extension for that language as well.

### Eclipse

    Just load the root `pom.xml`

## Bugs and Feature requests

You can register bugs and feature requests in the [Github Issue Tracker](https://github.com/cucumber/cucumber-jvm/issues)

You're most likely going to paste code and output, so familiarise yourself with [Github Flavored Markdown](http://github.github.com/github-flavored-markdown/) to make sure it remains readable.

Please consider including the following information if you register a ticket:

* What cucumber-jvm version you're using
* What modules you're using (`cucumber-java`, `cucumber-spring`, `cucumber-groovy` etc)
* What command you ran
* What output you saw
* How it can be reproduced

### How soon will my ticket be fixed?

The best way to have a bug fixed or feature request implemented is to [fork the cucumber-jvm repo](http://help.github.com/fork-a-repo/) and send a [pull request](http://help.github.com/send-pull-requests/). If the pull request has good tests and follows the coding conventions (see below) it has a good chance of making it into the next release.

If you don't fix the bug yourself (or pay someone to do it for you), the bug might never get fixed. If it is a serious bug, other people than you might care enough to provide a fix.

In other words, there is no guarantee that a bug or feature request gets fixed. Tickets that are more than 6 months old are likely to be closed to keep the backlog manageable.

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

Please do *not* add @author tags - this project embraces collective code ownership. If you want to know who wrote some code, look in git.
When you are done, send a [pull request](http://help.github.com/send-pull-requests/).
If we get a pull request where an entire file is changed because of insignificant whitespace changes we cannot see what you have changed, and your contribution might get rejected.

### Running cross-platform Cucumber features

All Cucumber implementations (cucumber-ruby, cucumber-jvm, cucumber-js) share a common set of Cucumber features to 
ensure all implementations support the same basic features. To run these you need to clone the cucumber-tck repo into your cucumber-jvm working copy:

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

## Contributing fixes

Fork the repository on Github, clone it and send a pull request when you have fixed something. Please commit each feature/bugfix on a separate branch as this makes it easier for us to decide what to merge and what not to merge.

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
* README.md
* History.md

Run `git commit -am "Release X.Y.Z"`

Now release everything:

```
mvn release:clean
mvn --batch-mode -P release-sign-artifacts release:prepare -DautoVersionSubmodules=true -DdevelopmentVersion=1.0.10-SNAPSHOT
mvn -P release-sign-artifacts release:perform
```

## Code Coverage

Code coverage is collected mainly to identify code that can be deleted or needs to be tested better.
To generate a report, run:

```
COBERTURA_HOME=/some/where ./cobertura.sh
```

This technique to collect coverage for a multi-module Maven project is based on a
[blog post](http://thomassundberg.wordpress.com/2012/02/18/test-coverage-in-a-multi-module-maven-project/) by Thomas Sundberg.

## Migration from Cuke4Duke

### POM
For those of you that have run Cuke4Duke via Maven in the past, and perhaps have a bulk of feature files to migrate, here is a quick guide to getting setup.

The first step is getting the POM.xml in your project configured with the right artifacts. 

Example Cucumber-JVM + Maven + Groovy CLI
( http://pastebin.com/XEmhuxkK )

This configuration will get you the following features:

* Groovy-only: No Junit/Java code will be run
* Tags: Multiple tags cannot be passed in from a JVM argument at this time, but multiple items can be added to the POM. Note that ~@ignore is in here by default, as an example. In addition, you can provide -DtagArgs="@tagname" to run any tag
* Formats: the <format> property can be changed from 'pretty' to 'html', or 'progress'. 
 * If html format is used, the --out parameter must be provided and set to a folder (relative to target) to dump the reports
 * The previous example, with target/reports specified as the output dir: ( http://pastebin.com/GrWN3ULN )

### Project structure

Next you'll want to structure your feature and step definition files according to the Cucumber-JVM hierarchy (quite a bit different than Cuke4Duke in most cases)

```
src\
  test\
    resources\
      featurefile.feature
      com\
        yourcompany\
          stepdefinitions.groovy
```

### Step definition changes

The only initial difference that will need to be made is to switch the metaclass mixin:

```
this.metaClass.mixin(cuke4duke.GroovyDsl)
```
over to the Cucumber-JVM way...
```
this.metaclass.mixin(cucumber.runtime.groovy.Hooks)
this.metaclass.mixin(cucumber.runtime.groovy.EN) // utilize your language here
```

Past that, there may be slight differences in the groovy coding aspects, but nothing too earth shattering
//TODO: Quantify what it takes to shatter an earth

### Run it!

To run all features:

``` 
mvn clean test

```

To specify a tag:

``` 
mvn clean test -DtagArg="@mytag"

```
