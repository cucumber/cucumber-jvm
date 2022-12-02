Cucumber Spring
===============

Use Cucumber Spring to share state between steps in a scenario and access the
spring application context.

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

## Configuring the Test Application Context

To make Cucumber aware of your test configuration you can annotate a
configuration class on your glue path with `@CucumberContextConfiguration` and with one of the
following annotations: `@ContextConfiguration`, `@ContextHierarchy` or
`@BootstrapWith`. If you are using SpringBoot, you can annotate configuration
class with `@SpringBootTest`.

For example:
```java
package com.example.app;

import org.springframework.boot.test.context.SpringBootTest;

import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest(classes = TestConfig.class)
public class CucumberSpringConfiguration {

}
```

Note: Cucumber Spring uses Spring's `TestContextManager` framework internally.
As a result a single Cucumber scenario will mostly behave like a JUnit test.

For more information configuring Spring tests see:
 - [Spring Framework Documentation - Testing](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html)
 - [Spring Boot Features - Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-testing)

### Configuring multiple Test Application Contexts

Per execution Cucumber can only launch a single Test Application Contexts. To
use multiple different application contexts Cucumber must be executed multiple
times.

#### JUnit 4 / TestNG

```java
package com.example;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(glue = "com.example.application.one", features = "classpath:com/example/application.one")
public class ApplicationOneTest {

}
```

Repeat as needed.

#### JUnit 5 + JUnit Platform Suite

```java
package com.example;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@SelectClasspathResource("com/example/application/one")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.example.application.one")
public class ApplicationOneTest {

}
```

Repeat as needed.

## Accessing the application context

Components from the application context can be accessed by autowiring.
Annotate a field in your step definition class with `@Autowired`. 

```java
package com.example.app;

import org.springframework.beans.factory.annotation.Autowired;
import io.cucumber.java.en.Given;

public class MyStepDefinitions {

   @Autowired
   private MyService myService;

   @Given("feed back is requested from my service")
   public void feed_back_is_requested(){
      myService.requestFeedBack();
   }
}
```

## Sharing State 

Cucumber Spring creates an application context and uses Spring's
`TestContextManager` framework internally. All scenarios as well as all other
tests (e.g. JUnit) that use the same context configuration will share one
instance of the Spring application. This avoids an expensive startup time.

### Sharing state between steps

To prevent sharing test state between scenarios, beans containing glue code
(i.e. step definitions, hooks, ect) are bound to the `cucumber-glue` scope.

The `cucumber-glue` scope starts prior to a scenario and ends after a scenario.
All beans in this scope will be created before a scenario execution and
disposed at the end of it.

By using the `@ScenarioScope` annotation additional components can be added to
the glue scope. These components can be used to safely share state between
steps inside a scenario. 

```java
package com.example.app;

import org.springframework.stereotype.Component;
import io.cucumber.spring.ScenarioScope;

@Component
@ScenarioScope
public class TestUserInformation {

    private User testUser;

    public void setTestUser(User testUser) {
        this.testUser = testUser;
    }

    public User getTestUser() {
        return testUser;
    }

}
```

The glue scoped component can then be autowired into a step definition:

```java
package com.example.app;

import org.springframework.beans.factory.annotation.Autowired;
import io.cucumber.java.en.Given;

public class UserStepDefinitions {

   @Autowired
   private UserService userService;

   @Autowired
   private TestUserInformation testUserInformation;

   @Given("there is a user")
   public void there_is_as_user() {
      User testUser = userService.createUser();
      testUserInformation.setTestUser(testUser);
   }
}

public class PurchaseStepDefinitions {

   @Autowired
   private PurchaseService purchaseService;

   @Autowired
   private TestUserInformation testUserInformation;

   @When("the user makes a purchase")
   public void the_user_makes_a_purchase(){
      Order order = ....
      User user = testUserInformation.getTestUser();
      purchaseService.purchase(user, order);
   }
}
```

#### Sharing state between threads

By default, when using `@ScenarioScope` these beans must also be accessed on
the  same thread as the one that is executing the scenario. If you are certain
your scenario scoped beans can only be accessed through step definitions you
can use `@ScenarioScope(proxyMode = ScopedProxyMode.NO)`.


```java
package com.example.app;

import org.springframework.stereotype.Component;
import io.cucumber.spring.ScenarioScope;
import org.springframework.context.annotation.ScopedProxyMode;

@Component
@ScenarioScope(proxyMode = ScopedProxyMode.NO)
public class TestUserInformation {

    private User testUser;

    public void setTestUser(User testUser) {
        this.testUser = testUser;
    }

    public User getTestUser() {
        return testUser;
    }

}
```

```java
package com.example.app;

import org.springframework.beans.factory.annotation.Autowired;
import io.cucumber.java.en.Given;
import org.awaitility.Awaitility;

public class UserStepDefinitions {

    @Autowired
    private TestUserInformation testUserInformation;

    @Then("the test user is eventually created")
    public void a_user_is_eventually_created() {
        Awaitility.await()
                .untilAsserted(() -> {
                    // This happens on a different thread
                    TestUser testUser = testUserInformation.getTestUser();
                    Optional<User> user = repository.findById(testUser.getId());
                    assertTrue(user.isPresent());
                });
    }
}
```



### Dirtying the application context

If your tests do dirty the application context you can add `@DirtiesContext` to 
your test configuration. 

```java
package com.example.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.test.context.SpringBootTest;

import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest(classes = TestConfig.class)
@DirtiesContext
public class CucumberSpringConfiguration {
   
}
```
```java
package com.example.app;

public class MyStepDefinitions {

   @Autowired
   private MyService myService;  // Each scenario have a new instance of MyService

}
```

Note: Using `@DirtiesContext` in combination with parallel execution will lead
to undefined behaviour.
