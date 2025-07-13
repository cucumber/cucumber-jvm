Cucumber CDI 2
==============

Use CDI Standalone Edition (CDI SE) API to provide dependency injection into
steps definitions.

Add the `cucumber-cdi2` dependency to your `pom.xml`
and use the [`cucumber-bom`](../cucumber-bom/README.md) for dependency management:

```xml
<dependencies>
  [...]
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-cdi2</artifactId>
        <scope>test</scope>
    </dependency>
  [...]
</dependencies>
```

## Setup

To use it, it is important to provide your CDI SE implementation - likely Weld or Apache OpenWebBeans.

For Apache OpenWebBeans the dependency is:

```xml
<dependency>
  <groupId>org.apache.openwebbeans</groupId>
  <artifactId>openwebbeans-se</artifactId>
  <version>2.0.10</version>
  <scope>test</scope>
</dependency>

```

And for Weld it is:

```xml
<dependency>
  <groupId>org.jboss.weld.se</groupId>
  <artifactId>weld-se-core</artifactId>
  <version>3.1.6.Final</version>
  <scope>test</scope>
</dependency>
```

## Usage

For each scenario, a new CDI container is started. If not present in the
container, step definitions are added as unmanaged beans and dependencies are
injected.

Note: Only step definition classes are added as unmanaged beans if not explicitly
defined. Other support code is not. Consider adding a `beans.xml` to
automatically declare test all classes as beans. 

Note: To share state step definitions and other support code must at least be
application scoped.

```java
package com.example.app;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StepDefinition {

    @Inject
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
