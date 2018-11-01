package io.cucumber.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import io.cucumber.guice.GuiceFactory;
import io.cucumber.java.api.ObjectFactory;
import io.cucumber.guice.api.CucumberModules;
import io.cucumber.guice.api.CucumberScopes;
import io.cucumber.guice.api.ScenarioScoped;
import io.cucumber.guice.matcher.ElementsAreAllEqualMatcher;
import io.cucumber.guice.matcher.ElementsAreAllUniqueMatcher;
import cucumber.api.guice.CucumberModules;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;

public class GuiceFactoryTest {

    @Rule
    public ExpectedException expectedException = none();

    private ObjectFactory factory;
    private List<?> instancesFromSameScenario;
    private List<?> instancesFromDifferentScenarios;

    @After
    public void tearDown() {
        // If factory is left in start state it can cause cascading failures due to scope being left open
        try { factory.stop(); } catch (Exception e) {}
    }

    @Test
    public void factoryCanBeIntantiatedWithDefaultConstructor() {
        factory = new GuiceFactory();
        assertThat(factory, notNullValue());
    }

    @Test
    public void factoryCanBeIntantiatedWithArgConstructor() {
        factory = new GuiceFactory(Guice.createInjector());
        assertThat(factory, notNullValue());
    }

    @Test
    public void factoryStartFailsIfScenarioScopeIsNotBound() {
        factory = new GuiceFactory(Guice.createInjector());
        expectedException.expectMessage(containsString("No implementation for io.cucumber.guice.api.ScenarioScope was bound"));
        factory.start();
    }

    static class UnscopedClass {
    }

    @Test
    public void shouldGiveNewInstancesOfUnscopedClassWithinAScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, UnscopedClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
    }

    @Test
    public void shouldGiveNewInstanceOfUnscopedClassForEachScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, UnscopedClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
    }

    @ScenarioScoped
    static class AnnotatedScenarioScopedClass {
    }

    @Test
    public void shouldGiveTheSameInstanceOfAnnotatedScenarioScopedClassWithinAScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, AnnotatedScenarioScopedClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    public void shouldGiveNewInstanceOfAnnotatedScenarioScopedClassForEachScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, AnnotatedScenarioScopedClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
    }

    @Singleton
    static class AnnotatedSingletonClass {
    }

    @Test
    public void shouldGiveTheSameInstanceOfAnnotatedSingletonClassWithinAScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, AnnotatedSingletonClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    public void shouldGiveTheSameInstanceOfAnnotatedSingletonClassForEachScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, AnnotatedSingletonClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    static class BoundScenarioScopedClass {
    }

    final AbstractModule boundScenarioScopedClassModule = new AbstractModule() {
        @Override
        protected void configure() {
            bind(BoundScenarioScopedClass.class).in(ScenarioScoped.class);
        }
    };

    @Test
    public void shouldGiveTheSameInstanceOfBoundScenarioScopedClassWithinAScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule(), boundScenarioScopedClassModule));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, BoundScenarioScopedClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    public void shouldGiveNewInstanceOfBoundScenarioScopedClassForEachScenario() {
        factory = new GuiceFactory(injector(CucumberModules.SCENARIO, boundScenarioScopedClassModule));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, BoundScenarioScopedClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
    }

    static class BoundSingletonClass {
    }

    final AbstractModule boundSingletonClassModule = new AbstractModule() {
        @Override
        protected void configure() {
            bind(BoundSingletonClass.class).in(Scopes.SINGLETON);
        }
    };

    @Test
    public void shouldGiveTheSameInstanceOfBoundSingletonClassWithinAScenario() {
        factory = new GuiceFactory(injector(CucumberModules.SCENARIO, boundSingletonClassModule));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, BoundSingletonClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    public void shouldGiveTheSameInstanceOfBoundSingletonClassForEachScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule(), boundSingletonClassModule));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, BoundSingletonClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    public void shouldGiveNewInstanceOfAnnotatedSingletonClassForEachScenarioWhenOverridingModuleBindingIsScenarioScope() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(AnnotatedSingletonClass.class).in(ScenarioScoped.class);
            }
        }));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, AnnotatedSingletonClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
    }

    private Injector injector(Module... module) {
        return Guice.createInjector(Stage.PRODUCTION, module);
    }


    private <E> List<E> getInstancesFromSameScenario(ObjectFactory factory, Class<E> aClass) {

        // Scenario
        factory.start();
        E o1 = factory.getInstance(aClass);
        E o2 = factory.getInstance(aClass);
        E o3 = factory.getInstance(aClass);
        factory.stop();

        return Arrays.asList(o1, o2, o3);
    }

    private <E> List<E> getInstancesFromDifferentScenarios(ObjectFactory factory, Class<E> aClass) {

        // Scenario 1
        factory.start();
        E o1 = factory.getInstance(aClass);
        factory.stop();

        // Scenario 2
        factory.start();
        E o2 = factory.getInstance(aClass);
        factory.stop();

        // Scenario 3
        factory.start();
        E o3 = factory.getInstance(aClass);
        factory.stop();

        return Arrays.asList(o1, o2, o3);
    }

}
