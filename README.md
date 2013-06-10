[![Build Status](https://secure.travis-ci.org/cucumber/cucumber-jvm.png)](http://travis-ci.org/cucumber/cucumber-jvm)

Cucumber-JVM is a pure Java implementation of Cucumber that supports the [most popular](http://cukes.info/platforms.html) programming languages for the JVM.

You can [run](http://cukes.info/running.html) it with the tool of your choice.

Cucumber-JVM also integrates with all the popular [Dependency Injection containers](http://cukes.info/install-cucumber-jvm.html).

## Documentation

[Start Here](http://cukes.info/platforms.html). This page also links to examples.
Look [here](http://cukes.info/api/cucumber/jvm/) for API docs.

## Hello World

Check out the simple [Hello World](https://github.com/cucumber/cucumber-jvm/tree/master/examples/java-helloworld) example.

## Downloading / Installation

[Install](http://cukes.info/install-cucumber-jvm.html)

## Bugs and Feature requests

You can register bugs and feature requests in the [Github Issue Tracker](https://github.com/cucumber/cucumber-jvm/issues).

You're most likely going to paste code and output, so familiarise yourself with
[Github Flavored Markdown](http://github.github.com/github-flavored-markdown/) to make sure it remains readable.

*At the very least - use triple backticks*:

<pre>
```java
// Why doesn't this work?
@Given("I have 3 cukes in my (.*)")
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

See [Contributing](http://cukes.info/contribute.html) as well as [CONTRIBUTING.md](https://github.com/cucumber/cucumber-jvm/blob/master/CONTRIBUTING.md)

## Coming from Cuke4Duke?

See [Migration from Cuke4Duke](https://github.com/cucumber/cucumber-jvm/blob/master/Cuke4Duke.md)
