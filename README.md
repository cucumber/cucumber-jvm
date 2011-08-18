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

## Hacking

To hack on Cucumber-JVM you need a JDK and a Ruby interpreter. Ruby is only used for code generation (Cucumber-JVM does not have any Ruby runtime dependencies). Both MRI and JRuby will do.

### Code generation

With Ruby installed and on your path, install some gems that are needed for code generation:

    gem install bundler
    bundle install

Now you can generate code:

    rake generate

### Building Cucumber-JVM

You'll need Maven to build the Java code (we're happily accepting patches for other build systems). To build and run tests, run:

    mvn clean install