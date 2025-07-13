Cucumber Guice
===============
The Cucumber Guice module allows you to use Google Guice dependency injection in your Cucumber tests. Guice comes as
standard with singleton scope and 'no scope'. This module adds Cucumber scenario scope to the scopes available for use
in your test code. The rest of this documentation assumes you have at least a basic understanding of Guice. Please refer
to the Guice wiki if necessary, see [Google Guice - Motivation](https://github.com/google/guice/wiki/Motivation)

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

## Migration from other versions
It's important to realise the differences in how this module functions when
compared with earlier versions. The changes are as follows.

### Version 1.1.7 and earlier
A Guice injector is created at the start of each test scenario and is
destroyed at the end of each test scenario. There is no scenario scope, just
singleton and 'no scope'.

### Version 1.1.8 onwards
A Guice injector is created once before any tests are run and is destroyed
after the last test has run. Before each test scenario, a new scenario scope
is created. At the end of the test scenario the scenario scope is destroyed.
Singleton scope exists throughout all test scenarios.

### Migrating to version 1.1.8 or later
Users wishing to migrate should replace `@Singleton` annotations
with `@ScenarioScope` annotations. Guice modules should also have
their singleton bindings updated. All bindings in
`Scopes.SINGLETON` should be replaced with bindings in
`CucumberScopes.SCENARIO`.


## Using the module
By including the `cucumber-guice` jar on your
`CLASSPATH` your Step Definitions will be instantiated by Guice.
There are two main modes of using the module: with [scope annotations](#scoping-your-step-definitions) and with
[module bindings](#using-module-bindings). The two modes can also be mixed. When mixing modes, it is
important to realise that binding a class in a scope in a module takes
precedence if the same class is also bound using a scope annotation.

An implementation of this interface is used to obtain an
`com.google.inject.Injector` that is used to provide instances of all the classes that are used to run the Cucumber
tests. The injector should be configured with a binding for `ScenarioScope`.

### Scoping your step definitions
Usually you will want to bind your step definition classes in either scenario
scope or in singleton scope. It is not recommended to leave your step
definition classes with no scope as it means that Cucumber will instantiate a
new instance of the class for each step within a scenario that uses that step
definition.

#### Scenario scope
Cucumber will create exactly one instance of a class bound in scenario scope
for each scenario in which it is used. You should use scenario scope when you
want to store state during a scenario but do not want the state to interfere
with subsequent scenarios.

#### Singleton scope
Cucumber will create just one instance of a class bound in singleton scope
that will last for the lifetime of all test scenarios in the test run. You
should use singleton scope if your classes are stateless. You can also use
singleton scope when your classes contain state but with caution. You should
be absolutely sure that a state change in one scenario could not possibly
influence the success or failure of a subsequent scenario. As an example of
when you might use a singleton, imagine you have an http client that is
expensive to create. By holding a reference to the client in a class bound in
singleton scope, you can reuse the client in multiple scenarios.

#### Using scope annotations
This is the easy route if you're new to Guice. To bind a class in scenario
scope add the `io.cucumber.guice.ScenarioScoped` annotation to the
class definition. The class should have a no-args constructor or one
constructor that is annotated with `javax.inject.Inject`. For
example:

```java
import cucumber.runtime.java.guice.ScenarioScoped;

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

To bind a class in singleton scope add the
`javax.inject.Singleton` annotation to the class definition. One
strategy for using stateless step definitions is to use providers to share
stateful scenario-scoped instances between stateless singleton step
definition instances. For example:

```java
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MyStatelessSteps {

    private final Provider<MyStatefulObject> providerMyStatefulObject;

    @Inject
    public MyStatelessSteps(Provider<MyStatefulObject> providerMyStatefulObject) {
        this.providerMyStatefulObject = providerMyStatefulObject;
    }

    @Given("^I have (\\d+) cukes in my belly$")
    public void I_have_cukes_in_my_belly(int n) {
        providerMyStatefulObject.get().iHaveCukesInMyBelly(n);
    }
}
```

There is an alternative explanation of using [providers for mixing scopes](https://github.com/google/guice/wiki/InjectingProviders#providers-for-mixing-scopes) on the Guice wiki.

### Using module bindings
As an alternative to using annotations you may prefer to declare Guice
bindings in a class that implements `com.google.inject.Module`. To
do this, you should create a class that implements
`io.cucumber.guice.api.InjectorSource`. This gives you complete
control over how you obtain a Guice injector and it's Guice modules. The
injector must provide a binding for
`io.cucumber.guice.ScenarioScope`. It should also provide a
binding for the `io.cucumber.guice.ScenarioScoped` annotation if
your classes are using the annotation. The easiest way to do this it to use
`CucumberModules.createScenarioModule()`. For example:

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
