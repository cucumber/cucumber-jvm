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

Final releases will be published in [Maven Central](http://search.maven.org/) when all issues in [Milestone 1](https://github.com/cucumber/cucumber-jvm/issues?milestone=1&state=open) are closed. Until then you can grab 
SNAPSHOT releases by adding this repo to your POM:

```xml
<repository>
    <id>sonatype-snapshots</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
</repository>
```

Now you can grab jars with the following dependency in your POM:

```xml
<dependency>
    <groupId>info.cukes</groupId>
    <artifactId>cucumber-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

If you are not using Maven you can download the SNAPSHOT jars manually from https://oss.sonatype.org/content/repositories/snapshots/info/cukes/

## Documentation

There isn't any documentation yet apart from API docs. Documentation will be published when the first release candidate for 1.0.0 is ready.
If you are adventurous, check out the examples, read the code and ask specific questions on the Cucumber mailing list.

### API Docs

* http://cukes.info/cucumber/jvm/api/1.0.0-SNAPSHOT/apidocs/ (URL subject to change)

## Examples

You will find an example in Git under examples/java-calculator. You should be able to run `basic_arithmetic.feature` by running the `cucumber.examples.java.calculator.basic_arithmetic_Test` JUnit test from your IDE. -Or simply by running it with Maven: `mvn clean install -P examples` once to build it all. Then `cd examples/java-calculator` followed by `mvn test` each time you make a change. Try to make the feature fail!

## Contributing/Hacking

To hack on Cucumber-JVM you need a JDK, Maven and Git to get the code. You also need to set your IDE/text editor to use:

* UTF-8 file encoding
* LF (UNIX) line endings
* 4 Space indent (no tabs)
  * Java
  * XML
* 2 Space indent (no tabs)
  * Gherkin

When you are done, send a [pull request](http://help.github.com/send-pull-requests/).
If we get a pull request where an entire file is changed because of insignificant whitespace changes we cannot see what you have changed, and your contribution might get rejected.

### Building Cucumber-JVM

You'll need Maven to build the Java code (we're happily accepting patches for other build systems). To build and run tests, run:

    mvn clean install

### Continuous Integration

http://jenkins-01.public.cifoundry.net/job/Cucumber%20JVM/

### Running cross-platform Cucumber features

All Cucumber implementations (cucumber-ruby, cucumber-jvm, cucumber-js) share a common set of Cucumber features to ensure all implementations support the same basic features. To run these you need to clone the cucumber-features into your cucumber-jvm working copy:

    git submodule update --init

Now you can run the cross-platform Cucumber features:

    gem install bundler
    bundle install
    rake

### Code generation

StepDefinition APIs in all of Gherkin's supported i18n languages are generated using Ruby. 
The i18n Java annotations (except English) are not added to the Git repo because Git on both OS X and Windows handles UTF-8 file names badly.
In order to compile `cucumber-java` with all I18n annotations, you have to generate them yourself.
With Ruby installed and on your path, install some gems that are needed for code generation:

#### Using bundler

Try this first

    gem install bundler
    bundle install

Now you can generate the code:

    rake generate

#### Without bundler

On Windows it might be tricky to install all the gems. (The listed gems are used for both code generation and for running the cross-platform features). If you only want to generate code, you can get away with:

    gem install gherkin
    rake generate SKIP_BUNDLER=true

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