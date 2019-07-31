Details of using DI (e.g. using Guice)
======================================

It is typically very simple to use Cucumber with a dependency injection framework.
All that's needed is to add the appropriate dependency to the classpath.

For example, if you want to use [Google Guice](https://github.com/google/guice) as DI
simply add the `cucumber-guice` dependency to your pom.xml:

```xml
<dependencies>
  [...]
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-guice</artifactId>
        <version>${cucumber.version}</version>
        <scope>test</scope>
    </dependency>
  [...]
</dependencies>
```

Now all your step definitions, hooks, transformers, etc. will be created and supplied by a Guice injector.
This is pretty cool, but depending on your application it is far off from being sufficient.

## The need for a custom injector

Even though example tests are very simple, they often do not stay that simple when it comes
to large applications contexts. An application often has specific components (data managers, providers,
services, etc.). These components need to be made available to your step definitions so that actions
can be applied on them and delivered results can be tested.

The reason using Cucumber with Guice DI typically originates from the fact that the tested application also uses
Guice as DI framework. So why not bringing this together and provide a custom injector that can inject
the application components into the step definitions.

```java
package com.example.app;

import static org.junit.Assert.assertTrue;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.guice.ScenarioScoped;
import com.example.app.service.AppService;
import java.util.Objects;
import javax.inject.Inject;

@ScenarioScoped
public final class StepDefinition {

    private final AppService appService;

    @Inject
    public StepDefinition( AppService appService ) {
        this.appService = Objects.requireNonNull( appService, "appService must not be null" );
    }

    @When("the application services are started")
    public void startServices() {
        this.appService.startServices();
    }

    @Then("all application services should be running")
    public void checkThatApplicationServicesAreRunning() {
        assertTrue( this.appService.servicesAreRunning() );
    }
}
```

This might not be working as expected. Why so?

In order for Guice to create the step definition, an instance of AppService is needed as argument for the constructor.
The implementation of the AppService may need further arguments and configuration that typically
has to be provided by a Guice module. Guice modules are used to configure an injector and might look like this: 

```java
package com.example.app.service.impl;

import com.example.app.service.AppService;
import com.google.inject.AbstractModule;

public final class ServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind( AppService.class ).to( AppServiceImpl.class );
        // ... (further bindings)
    }
}
```

The actual injector is then created like this: `injector = Guice.createInjector( new ServiceModule() );`

This means we need to have access to the creation of the injector that Cucumber creates in order to customize
it with our application specific modules.

## The Cucumber object factory

Whenever Cucumber needs a specific object, it asks an object factory for it.
Cucumber has a default object factory that (in case of Guice) creates a default injector and
delegates object creation to the injector.
In case we want to customize this injector we need to provide our own object factory and tell Cucumber to use it.

```java
package com.example.app;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.guice.CucumberModules;
import io.cucumber.guice.ScenarioScope;
import com.example.app.service.impl.ServiceModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

public final class CustomObjectFactory implements ObjectFactory {

    private Injector injector;

    public CustomObjectFactory() {
        // Create an injector with our service module
        this.injector =
          Guice.createInjector( Stage.PRODUCTION, CucumberModules.createScenarioModule(), new ServiceModule());
    }

    @Override
    public boolean addClass( Class< ? > clazz ) {
        return true;
    }

    @Override
    public void start() {
        this.injector.getInstance( ScenarioScope.class ).enterScope();
    }

    @Override
    public void stop() {
        this.injector.getInstance( ScenarioScope.class ).exitScope();
    }

    @Override
    public < T > T getInstance( Class< T > clazz ) {
        return this.injector.getInstance( clazz );
    }
}
```

This is almost the default object factory for Guice except that we have added our own bindings to the injector.

Cucumber loads the object factory through the `java.util.ServiceLoader`. In order for the ServiceLoader to be able
to pick up our custom implementation we need to provide the file `META-INF/services/io.cucumber.core.backend.ObjectFactory`.

```
com.example.app.CustomObjectFactory
#
# ... additional custom object factories could be added here
#
```

Our step definition should now be working except that we have to tell Cucumber to use our custom object factory.
There are several ways how this could be accomplished.

#### The command line

When Cucumber is run from the command line, the custom object factory can be specified as class name argument.

```bash
java io.cucumber.core.cli.Main --object-factory com.example.app.CustomObjectFactory
```

#### The property file

Cucumber makes use of a properties file (`cucumber.properties`) when it exists. The custom object factory can be
specified in this file and will be picked up when Cucumber is running. The following entry needs to be available
in the `cucumber.properties` file:

```
cucumber.object-factory=com.example.app.CustomObjectFactory
```

#### The test runner (JUnit/TestNG)

The Cucumber modules for JUnit and TestNG allow to run Cucumber through a JUnit/TestNG test. When the test is
executed it actually starts the Cucumber engine that runs the Cucumber tests. Cucumber can be configured using
the `@CucumberOptions` annotation. The custom object factory can be specified in this annotation.

```java
package com.example.app;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith( Cucumber.class )
@CucumberOptions( objectFactory = com.example.app.CustomObjectFactory.class )
public final class RunCucumberTest {
}
```

Using the `@CucumberOptions` annotation has another important advantage. It is possible to create different tests that
run different Cucumber scenarios (using tags). In case certain scenarios need a differently configured injector,
several custom object factories may exist and can be referenced by different tests.
This is e.g. very helpful when certain aspects of an application need to be tested with different configurations
or in different runtime environments.
