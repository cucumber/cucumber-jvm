# Documentation

While documentation is sparse, this document will serve as a placeholder so we remember what to document.

## Installation

Cucumber-JVM consists of several modules (jar files). 
What module to include in your project depends on the architecture of your app.

Examples:

* Java project that doesn't use a DI container: `cucumber-picocontainer` and `cucumber-junit`
* Java project that uses Spring: `cucumber-picocontainer` and `cucumber-junit`
* Clojure project: `cucumber-clojure`

You'll find a more detailed description of each module below.

TODO: Explain packaging with a [chord diagram](http://mbostock.github.com/d3/ex/chord.html) or similar.

## Core

* Transformation of capture groups to numbers etc. Refer to XStream docs.
* Table args - transformation to list of objects

## JUnit
## Java

* T

### PicoContainer
### Guice
### Spring
### Weld
## Groovy
## Clojure