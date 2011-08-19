Cucumber-JVM is a pure Java implementation of Cucumber that supports the following programming languages:

* Clojure
* Groovy
* Ioke
* Java
* JavaScript (Rhino)
* Scala

Cucumber-JVM provides the following mechanisms for running Cucumber Features:

* Ant
* Command Line
* JUnit (from an IDE)
* Maven

Cucumber-JVM also integrates with the following Dependency Injection containers:

* Guice
* PicoContainer
* Spring

## Downloading / Installation

There are no downloadable releases yet. See the Hacking section for installation instructions. Downloadable releases will be made available when the time is right.

## Examples

You will find an example under examples/java-calculator. You should be able to run `basic_arithmetic.feature` by running the `cucumber.examples.java.calculator.basic_arithmetic_Test` JUnit test from your IDE. -Or simply by running it with Maven: `mvn clean install -P examples` once to build it all. Then `cd examples/java-calculator` followed by `mvn test` each time you make a change. Try to make the feature fail!

## Hacking

To hack on Cucumber-JVM you need a JDK, Maven and Git to get the code.

### Building Cucumber-JVM

You'll need Maven to build the Java code (we're happily accepting patches for other build systems). To build and run tests, run:

    mvn clean install

### Running cross-platform Cucumber features

All Cucumber implementations (cucumber-ruby, cucumber-jvm, cucumber-js) share a common set of Cucumber features to ensure all implementations support the same basic features. To run these you need to clone the cucumber-features into your cucumber-jcm working copy:

    git submodule update --init

Now you can run the cross-platform Cucumber features:

    rake

### Code generation

StepDefinition APIs in all of Gherkin's supported i18n languages are generated using Ruby. 
The i18n Java annotations (except English) are not added to the Git repo because Git on both OS X and Windows handles UTF-8 file names badly.
In order to compile `cucumber-java` with all I18n annotations, you have to generate them yourelf.
With Ruby installed and on your path, install some gems that are needed for code generation:

    gem install bundler
    bundle install

Now you can generate the code:

    rake generate

## Troubleshooting

Below are some common problems you might encounter while hacking on Cucumber-JVM - and solutions.

### IntelliJ Idea fails to compile the generated I18n Java annotations

This can be solved by changing the Compiler settings: `Preferences -> Compiler -> Java Compiler`:

* *Use compiler:* `Javac in-process (Java6+ only)`
* *Additional command line parameters:* `-target 1.6 -source 1.6 -encoding UTF-8`

## Contributing fixes

Fork the repository on Github, clone it and send a pull request when you have fixed something. Please commit each feature/bugfix on a separate branch as this makes it easier for us to decide what to merge and what not to merge.

