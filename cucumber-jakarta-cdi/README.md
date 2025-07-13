Cucumber CDI Jakarta
====================

Use CDI Standalone Edition (CDI SE) API to provide dependency injection in to
steps definitions.

Add the `cucumber-jakarta-cdi` dependency to your `pom.xml`
and use the [`cucumber-bom`](../cucumber-bom/README.md) for dependency management:

```xml
<dependencies>
  [...]
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-jakarta-cdi</artifactId>
        <scope>test</scope>
    </dependency>
  [...]
</dependencies>
```

IMPORTANT: This module uses jakarta flavor of CDI and not javax one.

## Setup

To use it, it is important to provide your CDI SE implementation - likely Weld
or Apache OpenWebBeans.

#### Apache OpenWebBeans

Note: This example isn't up-to-date anymore. I don't know enough about
OpenWebBeans to keep it up to date. Please do send a pull request if you know.

```xml
<dependency>
    <groupId>org.apache.xbean</groupId>
    <artifactId>xbean-finder-shaded</artifactId>
    <version>${xbean.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.apache.xbean</groupId>
    <artifactId>xbean-asm7-shaded</artifactId> <!-- or asm8 flavor for more recent openwebbeans -->
    <version>${xbean.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.apache.openwebbeans</groupId>
    <artifactId>openwebbeans-impl</artifactId>
    <version>${openwebbeans.version}</version>
    <scope>test</scope>
    <classifier>jakarta</classifier>
    <exclusions>
        <exclusion>
            <groupId>*</groupId>
            <artifactId>*</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.apache.openwebbeans</groupId>
    <artifactId>openwebbeans-spi</artifactId>
    <version>${openwebbeans.version}</version>
    <scope>test</scope>
    <classifier>jakarta</classifier>
    <exclusions>
        <exclusion>
            <groupId>*</groupId>
            <artifactId>*</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.apache.openwebbeans</groupId>
    <artifactId>openwebbeans-se</artifactId>
    <version>${openwebbeans.version}</version>
    <classifier>jakarta</classifier>
    <scope>test</scope>
    <exclusions>
        <exclusion>
            <groupId>*</groupId>
            <artifactId>*</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

#### Weld

```xml
<dependency>
  <groupId>org.jboss.weld.se</groupId>
  <artifactId>weld-se-core</artifactId>
  <version>4.0.0</version>
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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
