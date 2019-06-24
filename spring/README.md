Cucumber Spring
===============

Use Cucumber Spring to manage state between steps and for scenarios.

Add the `cucumber-spring` dependency to your `pom.xml`:

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

* Add a `@Component` annotation to each of the classes cucumber-spring should manage.
* Add the location of your classes to the `@ComponentScan` of your (test) configuration:

```java
package com.example.app;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.example.app")
public class Config {
    // the rest of your configuration
}
```

* Add `@DirtiesContext` to your test configuration if each scenario should have a fresh application context.

For classes from other frameworks:

* You will have to explicitly register them as Beans in your (test) configuration:

```java
package com.example.app;

import other.framework.Class;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ComponentScan("com.example.app")
public class TestConfig {
    @Bean
    public SomeOtherService someOtherService() {
        // return an instance of some other service
    }
}
```

To make Cucumber aware of your test configuration you can annotate a single step definition with 
`@ContextConfiguration`, `@ContextHierarchy` or `@BootstrapWith`. If you are using SpringBoot, you can annotate a 
single step definition class with `@SpringBootTest(classes = TestConfig.class)`.

For example:
```java
import com.example.app;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(classes = TestConfig.class)
public class SomeServiceSteps {

    // the rest of your step definitions

}
```

Now you can use the registered beans by autowiring them where you need them.

For example:
```java
import com.example.app;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(classes = TestConfig.class)
public class SomeServiceSteps {
    
    @Autowired
    SomeService someService;

    @Autowired
    SomeOtherService someOtherService;

    // the rest of your step definitions
}
```

Changing a Spring bean's scope to `SCOPE_CUCUMBER_GLUE` will bound its lifecycle to the standard glue lifecycle.

```java
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class MyComponent {
}
```

### XML Configuration

If you are using xml based configuration, you can to register the beans in a `cucumber.xml` file:

```xml
<bean class="com.example.app.MyService"/>
<bean class="com.example.lib.SomeOtherService"/>
```

Annotate a single step definition class with `@ContextConfiguration("classpath:cucumber.xml")`

