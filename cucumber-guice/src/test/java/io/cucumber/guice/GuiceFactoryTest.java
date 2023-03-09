package io.cucumber.guice;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.resource.ClasspathSupport;
import io.cucumber.guice.factory.SecondInjectorSource;
import io.cucumber.guice.integration.YourInjectorSource;
import io.cucumber.guice.matcher.ElementsAreAllEqualMatcher;
import io.cucumber.guice.matcher.ElementsAreAllUniqueMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import javax.inject.Singleton;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void factoryCanBeInstantiatedWithDefaultConstructor() {
        ObjectFactory factory = new GuiceFactory();
        assertThat(factory, notNullValue());
    }

    @Test
    void factoryCanBeInstantiatedWithArgConstructor() {
        initFactory(Guice.createInjector());
        assertThat(factory, notNullValue());
    }

    @Test
    void factoryStartFailsIfScenarioScopeIsNotBound() {
        initFactory(Guice.createInjector());

        Executable testMethod = () -> factory.start();
        ConfigurationException actualThrown = assertThrows(ConfigurationException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(),
            containsString("1) [Guice/MissingImplementation]: No implementation for ScenarioScope was bound."));
    }

    @Test
    void shouldGiveNewInstancesOfUnscopedClassWithinAScenario() {
        initFactory(injector(CucumberModules.createScenarioModule()));
        List<?> instancesFromSameScenario = getInstancesFromSameScenario(factory, UnscopedClass.class);
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
        List<?> instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, UnscopedClass.class);
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
        List<?> instancesFromSameScenario = getInstancesFromSameScenario(factory, AnnotatedScenarioScopedClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveNewInstanceOfAnnotatedScenarioScopedClassForEachScenario() {
        initFactory(injector(CucumberModules.createScenarioModule()));
        List<?> instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory,
            AnnotatedScenarioScopedClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
    }

    @Test
    void shouldGiveTheSameInstanceOfAnnotatedSingletonClassWithinAScenario() {
        initFactory(injector(CucumberModules.createScenarioModule()));
        List<?> instancesFromSameScenario = getInstancesFromSameScenario(factory, AnnotatedSingletonClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveTheSameInstanceOfAnnotatedSingletonClassForEachScenario() {
        initFactory(injector(CucumberModules.createScenarioModule()));
        List<?> instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory,
            AnnotatedSingletonClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveTheSameInstanceOfBoundScenarioScopedClassWithinAScenario() {
        initFactory(injector(CucumberModules.createScenarioModule(), boundScenarioScopedClassModule));
        List<?> instancesFromSameScenario = getInstancesFromSameScenario(factory, BoundScenarioScopedClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveNewInstanceOfBoundScenarioScopedClassForEachScenario() {
        initFactory(injector(CucumberModules.createScenarioModule(), boundScenarioScopedClassModule));
        List<?> instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory,
            BoundScenarioScopedClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
    }

    @Test
    void shouldGiveTheSameInstanceOfBoundSingletonClassWithinAScenario() {
        initFactory(injector(CucumberModules.createScenarioModule(), boundSingletonClassModule));
        List<?> instancesFromSameScenario = getInstancesFromSameScenario(factory, BoundSingletonClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveTheSameInstanceOfBoundSingletonClassForEachScenario() {
        initFactory(injector(CucumberModules.createScenarioModule(), boundSingletonClassModule));
        List<?> instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory,
            BoundSingletonClass.class);
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
        List<AnnotatedSingletonClass> instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory,
            AnnotatedSingletonClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
    }

    @Test
    void shouldStartWhenInjectorSourceIsNull() {
        factory = new GuiceFactory();
        factory.start();
    }

    @Test
    void shouldAddInjectorSource() {
        factory = new GuiceFactory();
        assertTrue(factory.addClass(YourInjectorSource.class));
    }

    @Test
    void shouldReturnSameIfInjectorSourceIsFoundTwice() {
        factory = new GuiceFactory();
        assertTrue(factory.addClass(YourInjectorSource.class));
        assertTrue(factory.addClass(YourInjectorSource.class));
    }

    @Test
    void shouldThrowExceptionIfTwoDifferentInjectorSourcesAreFound() {
        factory = new GuiceFactory();
        assertTrue(factory.addClass(YourInjectorSource.class));

        Executable testMethod = () -> factory.addClass(SecondInjectorSource.class);
        CucumberBackendException actualThrown = assertThrows(CucumberBackendException.class, testMethod);
        String exceptionMessage = String.format("" +
                "Glue class %1$s and %2$s are both implementing io.cucumber.guice.InjectorSource.\n" +
                "Please ensure only one class configures the Guice context\n" +
                "\n" +
                "By default Cucumber scans the entire classpath for context configuration.\n" +
                "You can restrict this by configuring the glue path.\n" +
                ClasspathSupport.configurationExamples(),
            SecondInjectorSource.class,
            YourInjectorSource.class);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(exceptionMessage));
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
