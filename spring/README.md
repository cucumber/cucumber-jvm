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

* Add a `@Component` annotation to each of the classes `cucumber-spring` should
manage.
```java
package com.example.app;

import org.springframework.stereotype.Component;

@Component
public class Belly {
    private int cukes = 0;

    public void setCukes(int cukes) {
        this.cukes = cukes;
    }

    public int getCukes() {
        return cukes;
    }
}
```
* Add the location of your classes to the `@ComponentScan` of your (test)
configuration:

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

For classes from other frameworks:

* You will have to explicitly register them as Beans in your (test) configuration:

```java
package com.example.app;

import com.example.other.framework.SomeOtherService;
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

To make Cucumber aware of your test configuration you can annotate a single step
definition with `@ContextConfiguration`, `@ContextHierarchy` or
`@BootstrapWith`. If you are using SpringBoot, you can annotate a single step
definition class with `@SpringBootTest(classes = TestConfig.class)`.

For example:
```java
import com.example.app;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestConfig.class)
public class SomeServiceStepDefinitions {

    // the rest of your step definitions

}
```

Now you can use the registered beans by autowiring them where you need them.

For example:
```java
import com.example.app;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(classes = TestConfig.class)
public class SomeServiceStepDefinitions {
    
    @Autowired
    SomeService someService;

    @Autowired
    SomeOtherService someOtherService;

    // the rest of your step definitions
}
```

### The Application Context & Cucumber Glue Scope

Cucumber Spring creates an application context. This application context is
shared between scenarios.

To prevent sharing state between scenarios, beans containing glue code
(i.e. step definitions, hooks, ect) are bound to the `cucumber-glue` scope.

The `cucumber-glue` scope starts prior to a scenario and end after a scenario.
Beans in this scope are created prior to a scenario execution and disposed at
the end of it.

Changing a Spring bean's scope to `SCOPE_CUCUMBER_GLUE` will bind its lifecycle
to the `cucumber-glue` scope.

```java
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Component
@Scope(SCOPE_CUCUMBER_GLUE)
public class MyComponent {
}
```

If your tests do dirty the application context you can add `@DirtiesContext` to 
your test configuration.

```java
package com.example.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestConfig.class)
@DirtiesContext
public class SomeServiceStepDefinitions {

    @Autowired
    private Belly belly; // Each scenario have a new instance of Belly
    
    [...]
    
}
```

### XML Configuration

If you are using xml based configuration, you can to register the beans in a
`cucumber.xml` file:

```xml
<bean class="com.example.app.MyService"/>
<bean class="com.example.lib.SomeOtherService"/>
```

Annotate a single step definition class with 
`@ContextConfiguration("classpath:cucumber.xml")`

