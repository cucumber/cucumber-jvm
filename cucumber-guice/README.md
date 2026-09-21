Cucumber Guice
===============

The Cucumber Guice module allows you to use Google Guice dependency injection in
your Cucumber tests.

This module adds Cucumber scenario scope to the scopes available for use in your
test code. The rest of this documentation assumes you have at least a basic
understanding of Guice. Please refer to the Guice wiki if necessary, see
[Google Guice - Motivation](https://github.com/google/guice/wiki/Motivation)

Add the `cucumber-guice` dependency to your `pom.xml` and use
the [`cucumber-bom`](../cucumber-bom/README.md) for dependency management:

```xml

<dependencies>
	[...]
	<dependency>
		<groupId>io.cucumber</groupId>
		<artifactId>cucumber-guice</artifactId>
		<scope>test</scope>
	</dependency>
	[...]
</dependencies>
```

## Using the module

To bind a class in scenario scope add the `io.cucumber.guice.ScenarioScoped`
annotation to the class definition. The class should have a no-args constructor
or one constructor that is annotated with `javax.inject.Inject`. For example:

```java
import com.example.app;

import io.cucumber.guice.ScenarioScoped;
import javax.inject.Inject;

@ScenarioScoped
public class ScenarioScopedSteps {

    private final Object someInjectedDependency;

    @Inject
    public ScenarioScopedSteps(Object someInjectedDependency) {
        this.someInjectedDependency = someInjectedDependency;
    }
}
```

Cucumber will create exactly one instance of a class bound in scenario scope for
each scenario in which it is used. You should use scenario scope when you want
to store state during a scenario but do not want the state to interfere with
subsequent scenarios.

Note:
* When binding a class to the singleton scope Cucumber will create one
  instance of a class _per thread_ used to run Cucumber.
* It is not recommended to leave your step definition classes with no scope as
  it means that Cucumber will instantiate a new instance of the class for each
  step within a scenario that uses that step definition.
* Any injected dependencies should be either scenario or singleton scoped.

## Customizing the injector source

You may want to inject (parts-of) the application under test into your step
definitions. To do this, you should create a class that implements
`io.cucumber.guice.api.InjectorSource`. This gives you complete control over how
you obtain a Guice injector, and it's Guice modules.

The injector must provide a binding for `io.cucumber.guice.ScenarioScope`. It
should also provide a binding for the `io.cucumber.guice.ScenarioScoped`
annotation if your classes are using the annotation. The easiest way to do this
it to use `CucumberModules.createScenarioModule()`. For example:

```java
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import io.cucumber.guice.CucumberModules;
import io.cucumber.guice.InjectorSource;

public class YourInjectorSource implements InjectorSource {

    @Override
    public Injector getInjector() {
        return Guice.createInjector(Stage.PRODUCTION, CucumberModules.createScenarioModule(), new YourModule());
    }
}
```

### Parallel execution

As noted above, by default Cucumber will create one instance of a singleton class _per
thread_ used to run Cucumber. This may not be ideal when running in parallel as
it will duplicate expensive resources.

This can be avoided by creating a separate injector for the singletons and
creating a child injector with the scenario module from that injector.

```java
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import io.cucumber.guice.InjectorSource;
 
public class YourInjectorSource implements InjectorSource {
 
    //This injector needs to be static so it won't be duplicated in different threads
    private static final Injector singletonInjector = Guice.createInjector(Stage.PRODUCTION, new YourModule());
 
    @Override
    public Injector getInjector() {
        return singletonInjector.createChildInjector(CucumberModules.createScenarioModule());
    }
}
```
