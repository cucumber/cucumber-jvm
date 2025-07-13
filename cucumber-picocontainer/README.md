Cucumber PicoContainer
======================

Use PicoContainer to provide dependency injection to steps.

Add the `cucumber-picocontainer` dependency to your `pom.xml`
and use the [`cucumber-bom`](../cucumber-bom/README.md) for dependency management:

```xml
<dependencies>
  [...]
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-picocontainer</artifactId>
        <scope>test</scope>
    </dependency>
  [...]
</dependencies>
```

## Step dependencies

PicoContainer uses constructor dependency injection to create instances
of step definition classes and their dependencies.

In the example bellow to create an instance of `StepDefinition` an instance of
`Belly` is needed. So `Belly` is a dependency of `StepDefinition`. `Belly` has
no dependencies and can be created with the default constructor.

Once an instance of `Belly` has been created, this can be used to create an
instance of `StepDefinition`. This instance is then used to invoke the Given/
When/Then methods on.


```java
package com.example.app;

import java.util.List;

public class Belly {

    private final List<String> contents;

    public void setContents(List<String> contents){
        this.contents = contents;
    }
    
    public List<String> getContents(){
        return contents;
    }
}
```

```java
package com.example.app;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StepDefinition {

    private final Belly belly;

    public StepDefinitions(Belly belly) {
        this.belly = belly;
    }

    @Given("I have {int} {word} in my belly")
    public void I_have_n_things_in_my_belly(int n, String what) {
        belly.setContents(Collections.nCopies(n, what));
    }

    @Then("there are {int} cukes in my belly")
    public void checkCukes(int n) {
        assertEquals(belly.getContents(), Collections.nCopies(n, "cukes"));
    }
}
```

## Step scope and lifecycle

All step classes and their dependencies will be recreated for each
scenario, even if the scenario in question does not use any steps from
that particular class.

To improve performance, it is recommended to lazily create expensive
resources.

```java
public class LazyWebDriver implements Webdriver {

    private final Webdriver delegate;

    private Webdriver getDelegate() {
        if (delegate == null) {
            delegate = new ChromeWebDriver();
        } 
        return webdriver;
    }

    @Override
    public void doThing() {
        getDelegate().doThing();
    }
   
   ...
}
```

Step classes or their dependencies which own resources requiring cleanup
should implement `org.picocontainer.Disposable` as described in
[PicoContainer - Component Lifecycle](http://picocontainer.com/lifecycle.html).
These hooks will run after any Cucumber after hooks.

## Customizing PicoContainer

Cucumber `PicoFactory` is intentionally not open for extension or
customization. If you want to customize your dependency injection context,
it is recommended to provide your own implementation of 
`io.cucumber.core.backend.ObjectFactory` and make it available through
SPI.
