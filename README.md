# Cucumber JVM

[![OpenCollective](https://opencollective.com/cucumber/backers/badge.svg)](https://opencollective.com/cucumber) 
[![OpenCollective](https://opencollective.com/cucumber/sponsors/badge.svg)](https://opencollective.com/cucumber)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cucumber/cucumber-jvm/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cucumber/cucumber-jvm)
[![Build Status](https://secure.travis-ci.org/cucumber/cucumber-jvm.svg)](http://travis-ci.org/cucumber/cucumber-jvm)
[![Coverage Status](https://coveralls.io/repos/github/cucumber/cucumber-jvm/badge.svg?branch=master)](https://coveralls.io/github/cucumber/cucumber-jvm?branch=master)

Cucumber-JVM is a pure Java implementation of Cucumber that supports the [most popular programming languages](https://cucumber.io/docs/reference/jvm#running) for the JVM.

You can [run](https://cucumber.io/docs/reference/jvm#running) it with the tool of your choice.

Cucumber-JVM also integrates with all the popular [Dependency Injection containers](https://cucumber.io/docs/reference/java-di).

Some JVM languages have been moved to their own repository:
* [Clojure](https://github.com/cucumber/cucumber-jvm-clojure)
* [Gosu](https://github.com/cucumber/cucumber-jvm-gosu)
* [Groovy](https://github.com/cucumber/cucumber-jvm-groovy)
* [JRuby](https://github.com/cucumber/cucumber-jvm-jruby)
* [Jython](https://github.com/cucumber/cucumber-jvm-jython)
* [Rhino](https://github.com/cucumber/cucumber-jvm-rhino)
* [Scala](https://github.com/cucumber/cucumber-jvm-scala)

## Documentation

[Start Here](https://cucumber.io/docs).

If you'd like to contribute to the documentation, go [here](https://github.com/cucumber/docs.cucumber.io).

## Hello World

Check out the simple [cucumber-java-skeleton](https://github.com/cucumber/cucumber-java-skeleton) starter project.

## Downloading / Installation

[Install](https://cucumber.io/docs/reference/jvm#installation)

## Bugs and Feature requests

You can register bugs and feature requests in the [Github Issue Tracker](https://github.com/cucumber/cucumber-jvm/issues).

You're most likely going to paste code and output, so familiarise yourself with
[Github Flavored Markdown](https://help.github.com/articles/github-flavored-markdown) to make sure it remains readable.

*At the very least - use triple backticks*:

<pre>
```java
// Why doesn't this work?
@Given("I have {int} cukes in my {string}")
public void some_cukes(int howMany, String what) {
    // HALP!
}
```
</pre>

Please consider including the following information if you register a ticket:

* What cucumber-jvm version you're using
* What modules you're using (`cucumber-java`, `cucumber-spring`, `cucumber-groovy` etc)
* What command you ran
* What output you saw
* How it can be reproduced

### How soon will my ticket be fixed?

The best way to have a bug fixed or feature request implemented is to
[fork the cucumber-jvm repo](http://help.github.com/fork-a-repo/) and send a
[pull request](http://help.github.com/send-pull-requests/).
If the pull request has good tests and follows the coding conventions (see below) it has a good chance of
making it into the next release.

If you don't fix the bug yourself (or pay someone to do it for you), the bug might never get fixed. If it is a serious
bug, other people than you might care enough to provide a fix.

In other words, there is no guarantee that a bug or feature request gets fixed. Tickets that are more than 6 months old
are likely to be closed to keep the backlog manageable.

## Contributing fixes

See [CONTRIBUTING.md](https://github.com/cucumber/cucumber-jvm/blob/master/CONTRIBUTING.md)
