package io.cucumber.guice;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.guice.matcher.ElementsAreAllEqualMatcher;
import io.cucumber.guice.matcher.ElementsAreAllUniqueMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import javax.inject.Singleton;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GuiceFactoryTest {

    final AbstractModule boundScenarioScopedClassModule = new AbstractModule() {
        @Override
        protected void configure() {
            bind(BoundScenarioScopedClass.class).in(ScenarioScoped.class);
        }
    };
    final AbstractModule boundSingletonClassModule = new AbstractModule() {
        @Override
        protected void configure() {
            bind(BoundSingletonClass.class).in(Scopes.SINGLETON);
        }
    };
    private ObjectFactory factory;
    private List<?> instancesFromSameScenario;
    private List<?> instancesFromDifferentScenarios;

    @AfterEach
    void tearDown() {
        // If factory is left in start state it can cause cascading failures due
        // to scope being left open
        try {
            factory.stop();
        } catch (Exception ignored) {
        }
    }

    private void initFactory(Injector injector) {
        this.factory = new GuiceFactory();
        if (injector != null)
            ((GuiceFactory) factory).setInjector(injector);
    }

    @Test
    void factoryCanBeIntantiatedWithDefaultConstructor() {
        factory = new GuiceFactory();
        assertThat(factory, notNullValue());
    }

    @Test
    void factoryCanBeIntantiatedWithArgConstructor() {
        initFactory(Guice.createInjector());
        assertThat(factory, notNullValue());
    }

    @Test
    void factoryStartFailsIfScenarioScopeIsNotBound() {
        initFactory(Guice.createInjector());

        Executable testMethod = () -> factory.start();
        ConfigurationException actualThrown = assertThrows(ConfigurationException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), startsWith(
            "Guice configuration errors:\n" +
                    "\n" +
                    "1) [Guice/MissingImplementation]: No implementation for ScenarioScope was bound."));
    }

    @Test
    void shouldGiveNewInstancesOfUnscopedClassWithinAScenario() {
        initFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, UnscopedClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
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

    @Test
    void shouldGiveNewInstanceOfUnscopedClassForEachScenario() {
        initFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, UnscopedClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
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

    @Test
    void shouldGiveTheSameInstanceOfAnnotatedScenarioScopedClassWithinAScenario() {
        initFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, AnnotatedScenarioScopedClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveNewInstanceOfAnnotatedScenarioScopedClassForEachScenario() {
        initFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory,
            AnnotatedScenarioScopedClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
    }

    @Test
    void shouldGiveTheSameInstanceOfAnnotatedSingletonClassWithinAScenario() {
        initFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, AnnotatedSingletonClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveTheSameInstanceOfAnnotatedSingletonClassForEachScenario() {
        initFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, AnnotatedSingletonClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveTheSameInstanceOfBoundScenarioScopedClassWithinAScenario() {
        initFactory(injector(CucumberModules.createScenarioModule(), boundScenarioScopedClassModule));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, BoundScenarioScopedClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveNewInstanceOfBoundScenarioScopedClassForEachScenario() {
        initFactory(injector(CucumberModules.createScenarioModule(), boundScenarioScopedClassModule));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, BoundScenarioScopedClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
    }

    @Test
    void shouldGiveTheSameInstanceOfBoundSingletonClassWithinAScenario() {
        initFactory(injector(CucumberModules.createScenarioModule(), boundSingletonClassModule));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, BoundSingletonClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveTheSameInstanceOfBoundSingletonClassForEachScenario() {
        initFactory(injector(CucumberModules.createScenarioModule(), boundSingletonClassModule));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, BoundSingletonClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveNewInstanceOfAnnotatedSingletonClassForEachScenarioWhenOverridingModuleBindingIsScenarioScope() {
        initFactory(injector(CucumberModules.createScenarioModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(AnnotatedSingletonClass.class).in(ScenarioScoped.class);
            }
        }));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, AnnotatedSingletonClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
    }

    static class UnscopedClass {

    }

    @ScenarioScoped
    static class AnnotatedScenarioScopedClass {

    }

    @Singleton
    static class AnnotatedSingletonClass {

    }

    static class BoundScenarioScopedClass {

    }

    static class BoundSingletonClass {

    }

}
