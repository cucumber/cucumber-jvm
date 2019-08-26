# Cucumber-JVM examples

Start with `java-helloworld` - it's the simplest example.

Some example projects depend on the current (unreleased) Cucumber-JVM modules.
If any of the examples fail to build, just build cucumber-jvm itself once first:

```
cd .. # the dir above this dir
mvn clean install
```

Any of the examples can be built and run with `mvn clean integration-test`. See individual `README.md` files for details.
