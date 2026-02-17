package io.cucumber.guice;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.resource.ClasspathSupport;
import io.cucumber.guice.factory.SecondInjectorSource;
import io.cucumber.guice.integration.YourInjectorSource;
import io.cucumber.guice.matcher.ElementsAreAllEqualMatcher;
import io.cucumber.guice.matcher.ElementsAreAllUniqueMatcher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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

    private @Nullable ObjectFactory factory;

    @AfterEach
    void tearDown() {
        // If factory is left in start state it can cause cascading failures due
        // to scope being left open
        if (factory != null) {
            factory.stop();
        }
    }

    private ObjectFactory initFactory(Injector injector) {
        this.factory = new GuiceFactory();
        if (injector != null)
            ((GuiceFactory) factory).setInjector(injector);
        return factory;
    }

    private ObjectFactory initFactory() {
        this.factory = new GuiceFactory();
        return factory;
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
        var factory = initFactory(Guice.createInjector());

        ConfigurationException actualThrown = assertThrows(ConfigurationException.class, factory::start);
        assertThat("Unexpected exception message", actualThrown.getMessage(),
            containsString("1) [Guice/MissingImplementation]: No implementation for ScenarioScope was bound."));
    }

    @Test
    void shouldGiveNewInstancesOfUnscopedClassWithinAScenario() {
        var factory = initFactory(injector(CucumberModules.createScenarioModule()));
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
        var factory = initFactory(injector(CucumberModules.createScenarioModule()));
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
        var factory = initFactory(injector(CucumberModules.createScenarioModule()));
        List<?> instancesFromSameScenario = getInstancesFromSameScenario(factory, AnnotatedScenarioScopedClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveNewInstanceOfAnnotatedScenarioScopedClassForEachScenario() {
        var factory = initFactory(injector(CucumberModules.createScenarioModule()));
        List<?> instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory,
            AnnotatedScenarioScopedClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
    }

    @Test
    void shouldGiveTheSameInstanceOfAnnotatedSingletonClassWithinAScenario() {
        var factory = initFactory(injector(CucumberModules.createScenarioModule()));
        List<?> instancesFromSameScenario = getInstancesFromSameScenario(factory, AnnotatedSingletonClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveTheSameInstanceOfAnnotatedSingletonClassForEachScenario() {
        var factory = initFactory(injector(CucumberModules.createScenarioModule()));
        List<?> instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory,
            AnnotatedSingletonClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveTheSameInstanceOfBoundScenarioScopedClassWithinAScenario() {
        var factory = initFactory(injector(CucumberModules.createScenarioModule(), boundScenarioScopedClassModule));
        List<?> instancesFromSameScenario = getInstancesFromSameScenario(factory, BoundScenarioScopedClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveNewInstanceOfBoundScenarioScopedClassForEachScenario() {
        var factory = initFactory(injector(CucumberModules.createScenarioModule(), boundScenarioScopedClassModule));
        List<?> instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory,
            BoundScenarioScopedClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllUniqueMatcher.elementsAreAllUnique());
    }

    @Test
    void shouldGiveTheSameInstanceOfBoundSingletonClassWithinAScenario() {
        var factory = initFactory(injector(CucumberModules.createScenarioModule(), boundSingletonClassModule));
        List<?> instancesFromSameScenario = getInstancesFromSameScenario(factory, BoundSingletonClass.class);
        assertThat(instancesFromSameScenario, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveTheSameInstanceOfBoundSingletonClassForEachScenario() {
        var factory = initFactory(injector(CucumberModules.createScenarioModule(), boundSingletonClassModule));
        List<?> instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory,
            BoundSingletonClass.class);
        assertThat(instancesFromDifferentScenarios, ElementsAreAllEqualMatcher.elementsAreAllEqual());
    }

    @Test
    void shouldGiveNewInstanceOfAnnotatedSingletonClassForEachScenarioWhenOverridingModuleBindingIsScenarioScope() {
        var factory = initFactory(injector(CucumberModules.createScenarioModule(), new AbstractModule() {
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
        var factory = initFactory();
        factory.start();
    }

    @Test
    void shouldAddInjectorSource() {
        var factory = initFactory();
        assertTrue(factory.addClass(YourInjectorSource.class));
    }

    @Test
    void shouldReturnSameIfInjectorSourceIsFoundTwice() {
        var factory = initFactory();
        assertTrue(factory.addClass(YourInjectorSource.class));
        assertTrue(factory.addClass(YourInjectorSource.class));
    }

    @Test
    void shouldThrowExceptionIfTwoDifferentInjectorSourcesAreFound() {
        var factory = initFactory();
        assertTrue(factory.addClass(YourInjectorSource.class));

        Executable testMethod = () -> factory.addClass(SecondInjectorSource.class);
        CucumberBackendException actualThrown = assertThrows(CucumberBackendException.class, testMethod);
        String exceptionMessage = """
                Glue class %%1$s and %%2$s are both implementing io.cucumber.guice.InjectorSource.
                Please ensure only one class configures the Guice context

                By default Cucumber scans the entire classpath for context configuration.
                You can restrict this by configuring the glue path.
                %s""".formatted(ClasspathSupport.configurationExamples())
                .formatted(SecondInjectorSource.class, YourInjectorSource.class);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(exceptionMessage));
    }

    @Test
    @SuppressWarnings("UnnecessaryAssignment")
    void shouldInjectStaticBeforeStart() {
        var factory = initFactory();
        WithStaticFieldClass.property = null;
        factory.addClass(CucumberInjector.class);
        assertThat(WithStaticFieldClass.property, equalTo("Hello world"));

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

    static class WithStaticFieldClass {

        @Inject
        static @Nullable String property;

    }

    public static class CucumberInjector implements InjectorSource {

        @Override
        public Injector getInjector() {
            return Guice.createInjector(Stage.PRODUCTION, CucumberModules.createScenarioModule(), new AbstractModule() {
                @Override
                protected void configure() {
                    requestStaticInjection(WithStaticFieldClass.class);
                }

                @Singleton
                @Provides
                public String providesSomeString() {
                    return "Hello world";
                }
            });
        }
    }

}
