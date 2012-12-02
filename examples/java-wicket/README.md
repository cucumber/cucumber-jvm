## A Wicket example

This is an example of a JEE application that requires deployment to a servlet container before it can be tested with
Selenium WebDriver and Cucumber-JVM using Java.

### Maven

Run:

```
mvn install
```

This runs Cucumber features using the JUnit runner. The `@RunWith(Cucumber.class)` annotation on the `RunCukesIT` junit class
kicks off Cucumber.

### Java 7

The cargo-maven2-plugin 1.3.0 is built using Java 7. This means that this example will fail if used with older Java versions.
The error will be similar to:

```
[ERROR] Failed to execute goal org.codehaus.cargo:cargo-maven2-plugin:1.3.0:start (start-servlet-engine) on project java-wicket-test: Execution start-servlet-engine of goal org.codehaus.cargo:cargo-maven2-plugin:1.3.0:start failed: An API incompatibility was encountered while executing org.codehaus.cargo:cargo-maven2-plugin:1.3.0:start: java.lang.UnsupportedClassVersionError: org/eclipse/jetty/server/Server : Unsupported major.minor version 51.0
```
