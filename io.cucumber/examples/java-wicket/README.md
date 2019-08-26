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
