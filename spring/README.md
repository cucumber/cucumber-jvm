Cucumber Spring
===============

Use Cucumber Spring to manage state between steps and for scenarios.

Add the `cucumber-spring` dependency to your pom.xml:

```xml
<dependencies>
  [...]
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-spring</artifactId>
        <version>${cucumber.version}</version>
        <scope>test</scope>
    </dependency>
  [...]
</dependencies>
```

## Annotation Based Configuration

For your own classes:

* Add the `@Component` annotation to each of the classes cucumber-spring should manage.

* Add a `@Scope("cucumber-glue")` annotation to have cucumber-spring remove them **after each scenario**

* Add the location of your classes to the `@ComponentScan` of your (test) configuration:

```java
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("your.package")
public class Config {
    // the rest of your configuration
}
```

For classes from other frameworks:

* You will have to explicitly register them as Beans in your (test) configuration:

```java
import other.framework.Class;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ComponentScan("your.package")
public class Config {
    @Bean
    @Scope("cucumber-glue")
    public Class otherClass() {
        // return an instance of the other class
    }
}
```

Now you can use the registered Beans by Autowiring them where you need them.

For example:
```java
import your.package.OtherStepDefs;
import org.springframework.beans.factory.annotation.Autowired;

public class StepDefs {
    @Autowired
    OtherStepDefs otherStepDefs;

    // the rest of your step definitions
}

```

## XML Configuration

If you are using xml based configuration, you can to register the beans in a `cucumber.xml` file:

```xml
<bean class="your.package.YourClass" scope="cucumber-glue" />
<bean class="other.framework.Class" scope="cucumber-glue" />
```

Annotate your StepDefinition class with `@ContextConfiguration("classpath:cucumber.xml")`

## SpringBoot

If you are using SpringBoot, you can annotate your StepDefinition class with `@SpringBootTest(classes = TestConfig.class)`.
