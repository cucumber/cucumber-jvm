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

Documentation and releases will come later. For now you have to build it yourself and read the code to understand what to do.

## Hacking

To hack on Cucumber-JVM you need a JDK and Maven. 

### Building Cucumber-JVM

You'll need Maven to build the Java code (we're happily accepting patches for other build systems). To build and run tests, run:

    mvn clean install

### Running cross-platform Cucumber features

All Cucumber implementations (cucumber-ruby, cucumber-jvm, cucumber-js) share a common set of Cucumber features to ensure all implementations support the same basic features. To run these you need to clone the cucumber-features into your cucumber-jcm working copy:

    git submodule update --init

Now you can run the cross-platform Cucumber features:

    rake

### Code generation

StepDefinition APIs in all of Gherkin's supported i18n languages are generated using Ruby. If a new Gherkin version is released (with i18n changes), the StepDefinition APIs have to be regenerated. With Ruby installed and on your path, install some gems that are needed for code generation:

    gem install bundler
    bundle install

Now you can generate code:

    rake generate

The generated files are added to Git. This is contrary to common practice, but it simplifies development as occasional contributors will not have to install Ruby. The files change relatively rarely anyway.

## Contributing fixes

Fork the repository on Github, clone it and send a pull request when you have fixed something. Please commit each feature/bugfix on a separate branch as this makes it easier for us to decide what to merge and what not to merge.

