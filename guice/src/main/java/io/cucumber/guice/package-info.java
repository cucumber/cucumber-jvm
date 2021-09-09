/**
 * Cucumber Guice configuration Api
 * <p>
 * An implementation of this interface is used to obtain an
 * <code>com.google.inject.Injector</code> that is used to provide instances of
 * all the classes that are used to run the Cucumber tests. The injector should
 * be configured with a binding for <code>ScenarioScope</code>.
 * <p>
 * This module allows you to use Google Guice dependency injection in your
 * Cucumber tests. Guice comes as standard with singleton scope and 'no scope'.
 * This module adds Cucumber scenario scope to the scopes available for use in
 * your test code. The rest of this documentation assumes you have at least a
 * basic understanding of Guice. Please refer to the Guice wiki if necessary,
 * see
 * <a href="https://github.com/google/guice/wiki/Motivation" target="_parent">
 * https://github.com/google/guice/wiki/Motivation</a>
 * </p>
 * <h3>About scopes, injectors and migration from earlier versions</h3>
 * <p>
 * It's important to realise the differences in how this module functions when
 * compared with earlier versions. The changes are as follows.
 * </p>
 * <h4>Version 1.1.7 and earlier</h4>
 * <p>
 * A Guice injector is created at the start of each test scenario and is
 * destroyed at the end of each test scenario. There is no scenario scope, just
 * singleton and 'no scope'.
 * </p>
 * <h4>Version 1.1.8 onwards</h4>
 * <p>
 * A Guice injector is created once before any tests are run and is destroyed
 * after the last test has run. Before each test scenario a new scenario scope
 * is created. At the end of the test scenario the scenario scope is destroyed.
 * Singleton scope exists throughout all test scenarios.
 * </p>
 * <h4>Migrating to version 1.1.8 or later</h4>
 * <p>
 * Users wishing to migrate should replace <code>@Singleton</code> annotations
 * with <code>@ScenarioScope</code> annotations. Guice modules should also have
 * their singleton bindings updated. All bindings in
 * <code>Scopes.SINGLETON</code> should be replaced with bindings in
 * <code>CucumberScopes.SCENARIO</code>.
 * </p>
 * <h3>Using the module</h3>
 * <p>
 * By including the <code>cucumber-guice</code> jar on your
 * <code>CLASSPATH</code> your Step Definitions will be instantiated by Guice.
 * There are two main modes of using the module: with scope annotations and with
 * module bindings. The two modes can also be mixed. When mixing modes it is
 * important to realise that binding a class in a scope in a module takes
 * precedence if the same class is also bound using a scope annotation.
 * </p>
 * <h3>Scoping your step definitions</h3>
 * <p>
 * Usually you will want to bind your step definition classes in either scenario
 * scope or in singleton scope. It is not recommended to leave your step
 * definition classes with no scope as it means that Cucumber will instantiate a
 * new instance of the class for each step within a scenario that uses that step
 * definition.
 * </p>
 * <h3>Scenario scope</h3>
 * <p>
 * Cucumber will create exactly one instance of a class bound in scenario scope
 * for each scenario in which it is used. You should use scenario scope when you
 * want to store state during a scenario but do not want the state to interfere
 * with subsequent scenarios.
 * </p>
 * <h3>Singleton scope</h3>
 * <p>
 * Cucumber will create just one instance of a class bound in singleton scope
 * that will last for the lifetime of all test scenarios in the test run. You
 * should use singleton scope if your classes are stateless. You can also use
 * singleton scope when your classes contain state but with caution. You should
 * be absolutely sure that a state change in one scenario could not possibly
 * influence the success or failure of a subsequent scenario. As an example of
 * when you might use a singleton, imagine you have an http client that is
 * expensive to create. By holding a reference to the client in a class bound in
 * singleton scope you can reuse the client in multiple scenarios.
 * </p>
 * <h3>Using scope annotations</h3>
 * <p>
 * This is the easy route if you're new to Guice. To bind a class in scenario
 * scope add the <code>io.cucumber.guice.ScenarioScoped</code> annotation to the
 * class definition. The class should have a no-args constructor or one
 * constructor that is annotated with <code>javax.inject.Inject</code>. For
 * example:
 * </p>
 * 
 * <pre>
 * import cucumber.runtime.java.guice.ScenarioScoped;
 * import javax.inject.Inject;
 *
 * {@literal @}ScenarioScoped
 * public class ScenarioScopedSteps {
 *
 * private final Object someInjectedDependency;
 *
 * {@literal @}Inject
 * public ScenarioScopedSteps(Object someInjectedDependency) {
 * this.someInjectedDependency = someInjectedDependency;
 * }
 *
 * ...
 * }
 * </pre>
 * <p>
 * To bind a class in singleton scope add the
 * <code>javax.inject.Singleton</code> annotation to the class definition. One
 * strategy for using stateless step definitions is to use providers to share
 * stateful scenario scoped instances between stateless singleton step
 * definition instances. For example:
 * </p>
 * 
 * <pre>
 * import javax.inject.Inject;
 * import javax.inject.Singleton;
 *
 * {@literal @}Singleton
 * public class MyStatelessSteps {
 *
 * private final Provider&lt;MyStatefulObject&gt; providerMyStatefulObject;
 *
 * {@literal @}Inject
 * public MyStatelessSteps(Provider&lt;MyStatefulObject&gt; providerMyStatefulObject) {
 * this.providerMyStatefulObject = providerMyStatefulObject;
 * }
 *
 * {@literal @}Given("^I have (\\d+) cukes in my belly$")
 * public void I_have_cukes_in_my_belly(int n) {
 * providerMyStatefulObject.get().iHaveCukesInMyBelly(n);
 * }
 *
 * ...
 * }
 * </pre>
 * <p>
 * There is an alternative explanation of using <a href=
 * "https://github.com/google/guice/wiki/InjectingProviders#providers-for-mixing-scopes"
 * target="_parent"> providers for mixing scopes</a> on the Guice wiki.
 * </p>
 * <h3>Using module bindings</h3>
 * <p>
 * As an alternative to using annotations you may prefer to declare Guice
 * bindings in a class that implements <code>com.google.inject.Module</code>. To
 * do this you should create a class that implements
 * <code>io.cucumber.guice.api.InjectorSource</code>. This gives you complete
 * control over how you obtain a Guice injector and it's Guice modules. The
 * injector must provide a binding for
 * <code>io.cucumber.guice.ScenarioScope</code>. It should also provide a
 * binding for the <code>io.cucumber.guice.ScenarioScoped</code> annotation if
 * your classes are using the annotation. The easiest way to do this it to use
 * <code>CucumberModules.createScenarioModule()</code>. For example:
 * </p>
 * 
 * <pre>
 * import com.google.inject.Guice;
 * import com.google.inject.Injector;
 * import com.google.inject.Stage;
 * import io.cucumber.guice.CucumberModules;
 * import io.cucumber.guice.InjectorSource;
 *
 * public class YourInjectorSource implements InjectorSource {
 *
 * {@literal @}Override
 * public Injector getInjector() {
 * return Guice.createInjector(Stage.PRODUCTION, CucumberModules.createScenarioModule(), new YourModule());
 * }
 * }
 * </pre>
 * <p>
 * Cucumber needs to know where to find the
 * <code>io.cucumber.guice.api.InjectorSource</code> that it will use. You
 * should create a properties file called
 * {@value io.cucumber.core.options.Constants#CUCUMBER_PROPERTIES_FILE_NAME} and
 * place it in the root of the classpath. The file should contain a single
 * property key called <code>guice.injector-source</code> with a value equal to
 * the fully qualified name of the
 * <code>io.cucumber.guice.api.InjectorSource</code>. For example:
 * </p>
 * 
 * <pre>
 * guice.injector-source=com.company.YourInjectorSource
 * </pre>
 */
package io.cucumber.guice;
