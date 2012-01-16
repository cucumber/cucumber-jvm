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

## Downloading / Installation

Releases are published in [Maven Central](http://search.maven.org/)

### Getting jars

Jar files can be browsed and downloaded from [Maven Central] or https://oss.sonatype.org/content/repositories/releases/info/cukes/ 
(New releases will show up here immediately, while it takes a couple of hours to sync to MAven Central).

### Using Maven

Add a dependency in your [POM](http://maven.apache.org/pom.html):

```xml
<dependency>
    <groupId>info.cukes</groupId>
    <artifactId>cucumber-core</artifactId>
    <version>1.0.0-RC3</version>
</dependency>
```

### Using Ivy

Add a [dependency](http://ant.apache.org/ivy/history/latest-milestone/ivyfile/dependency.html) in your [ivy.xml](http://ant.apache.org/ivy/history/latest-milestone/ivyfile.html):

```xml
    <dependency org="info.cukes" name="cucumber-core" rev="1.0.0-RC3"/>
```

Since the artifacts are released to Maven Central, the default Ivy configuration should pull them down automatically.
Alternatively you can define your own resolver:

```xml
<ibiblio name="sonatype"
    m2compatible="true"
    usepoms="true"
    pattern="[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]"
    root="https://oss.sonatype.org/content/repositories/releases/info/cukes/"/>
```

## Documentation

There isn't any documentation yet apart from API docs. Documentation will be published when the first release candidate for 1.0.0 is ready.
If you are adventurous, check out the examples, read the code and ask specific questions on the Cucumber mailing list.

### API Docs

TODO: Fix this. The Ivy build doesn't upload them yet.

* http://cukes.info/cucumber/jvm/api/1.0.0-RC3/apidocs/ (URL subject to change)

## Examples

You will find an example in Git under examples/java-calculator. You should be able to run `basic_arithmetic.feature` by running the `cucumber.examples.java.calculator.basic_arithmetic_Test` JUnit test from your IDE. -Or simply by running it with Maven: `mvn clean install -P examples` once to build it all. Then `cd examples/java-calculator` followed by `mvn test` each time you make a change. Try to make the feature fail!

### Building Cucumber-JVM

You'll need Ant installed

    export ANT_OPTS=-XX:MaxPermSize=128m
    ant clean publish-local

This will compile everything, run JUnit tests and Cucumber scenarios - and finally install all jars in your local Maven repo.
Yep - that's Ant+Ivy building everything - and dropping it off locally where it can be picked up by Maven (if you are so inclined).

## IDE Setup

### IntelliJ IDEA

The top level directory has a `cucumber-jvm.ipr` project file that references a `cucumber-*.iml` files.
Just run ant once (see above) and install the [IvyIDEA](http://code.google.com/p/ivyidea/) plugin. 

Now, open the `cucumber-jvm.ipr` project and you should be good to go.

## Contributing/Hacking

To hack on Cucumber-JVM you need a JDK, Maven and Git to get the code. You also need to set your IDE/text editor to use:

* UTF-8 file encoding
* LF (UNIX) line endings
* 4 Space indent (no tabs)
  * Java
  * XML
* 2 Space indent (no tabs)
  * Gherkin

Please do *not* add @author tags - this project embraces collective code ownership. If you want to know who wrote some code, look in git.
When you are done, send a [pull request](http://help.github.com/send-pull-requests/).
If we get a pull request where an entire file is changed because of insignificant whitespace changes we cannot see what you have changed, and your contribution might get rejected.

### Continuous Integration

http://jenkins-01.public.cifoundry.net/job/Cucumber%20JVM/

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

## TODO

* Reports exception when Before hook fails
* Skips steps when before hook fails
