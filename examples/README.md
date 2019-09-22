# Cucumber-JVM examples

To start with the simplest example, please use the 
[cucumber-java-skeleton](https://github.com/cucumber/cucumber-java-skeleton).

Other examples can be found in this directory.

Some example projects depend on the current (unreleased) Cucumber-JVM modules.
If any of the examples fail to build, just build cucumber-jvm itself once first:

```
cd .. # the dir above this dir
mvn clean install
```

Any of the examples can be built and run with `mvn clean integration-test`. See individual `README.md` files for details.
