package cucumber.runtime.java.guice.impl;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import cucumber.api.guice.CucumberModules;
import cucumber.runtime.java.guice.ScenarioScoped;
import io.cucumber.core.backend.ObjectFactory;
import org.junit.After;
import org.junit.Test;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

import static cucumber.runtime.java.guice.matcher.ElementsAreAllEqualMatcher.elementsAreAllEqual;
import static cucumber.runtime.java.guice.matcher.ElementsAreAllUniqueMatcher.elementsAreAllUnique;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class GuiceFactoryTest {

    private ObjectFactory factory;
    private List<?> instancesFromSameScenario;
    private List<?> instancesFromDifferentScenarios;

    @After
    public void tearDown() {
        // If factory is left in start state it can cause cascading failures due to scope being left open
        try { factory.stop(); } catch (Exception e) {}
    }

    @Test
    public void factoryCanBeIntantiatedWithDefaultConstructor() throws Exception {
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
        try {
            factory.start();
            fail();
        } catch (ConfigurationException e) {
            assertThat(e.getMessage(),
                    containsString("No implementation for cucumber.runtime.java.guice.ScenarioScope was bound"));
        }
    }

    static class UnscopedClass {}

    @Test
    public void shouldGiveNewInstancesOfUnscopedClassWithinAScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, UnscopedClass.class);
        assertThat(instancesFromSameScenario, elementsAreAllUnique());
    }

    @Test
    public void shouldGiveNewInstanceOfUnscopedClassForEachScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, UnscopedClass.class);
        assertThat(instancesFromDifferentScenarios, elementsAreAllUnique());
    }

    @ScenarioScoped static class AnnotatedScenarioScopedClass {}

    @Test
    public void shouldGiveTheSameInstanceOfAnnotatedScenarioScopedClassWithinAScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, AnnotatedScenarioScopedClass.class);
        assertThat(instancesFromSameScenario, elementsAreAllEqual());
    }

    @Test
    public void shouldGiveNewInstanceOfAnnotatedScenarioScopedClassForEachScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, AnnotatedScenarioScopedClass.class);
        assertThat(instancesFromDifferentScenarios, elementsAreAllUnique());
    }

    @Singleton static class AnnotatedSingletonClass {}

    @Test
    public void shouldGiveTheSameInstanceOfAnnotatedSingletonClassWithinAScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, AnnotatedSingletonClass.class);
        assertThat(instancesFromSameScenario, elementsAreAllEqual());
    }

    @Test
    public void shouldGiveTheSameInstanceOfAnnotatedSingletonClassForEachScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule()));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, AnnotatedSingletonClass.class);
        assertThat(instancesFromDifferentScenarios, elementsAreAllEqual());
    }

    static class BoundScenarioScopedClass {}

    final AbstractModule boundScenarioScopedClassModule = new AbstractModule() {
        @Override protected void configure() {
            bind(BoundScenarioScopedClass.class).in(ScenarioScoped.class);
        }
    };

    @Test
    public void shouldGiveTheSameInstanceOfBoundScenarioScopedClassWithinAScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule(), boundScenarioScopedClassModule));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, BoundScenarioScopedClass.class);
        assertThat(instancesFromSameScenario, elementsAreAllEqual());
    }

    @Test
    public void shouldGiveNewInstanceOfBoundScenarioScopedClassForEachScenario() {
        factory = new GuiceFactory(injector(CucumberModules.SCENARIO, boundScenarioScopedClassModule));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, BoundScenarioScopedClass.class);
        assertThat(instancesFromDifferentScenarios, elementsAreAllUnique());
    }

    static class BoundSingletonClass {}

    final AbstractModule boundSingletonClassModule = new AbstractModule() {
        @Override protected void configure() {
            bind(BoundSingletonClass.class).in(Scopes.SINGLETON);
        }
    };

    @Test
    public void shouldGiveTheSameInstanceOfBoundSingletonClassWithinAScenario() {
        factory = new GuiceFactory(injector(CucumberModules.SCENARIO, boundSingletonClassModule));
        instancesFromSameScenario = getInstancesFromSameScenario(factory, BoundSingletonClass.class);
        assertThat(instancesFromSameScenario, elementsAreAllEqual());
    }

    @Test
    public void shouldGiveTheSameInstanceOfBoundSingletonClassForEachScenario() {
        factory = new GuiceFactory(injector(CucumberModules.createScenarioModule(), boundSingletonClassModule));
        instancesFromDifferentScenarios = getInstancesFromDifferentScenarios(factory, BoundSingletonClass.class);
        assertThat(instancesFromDifferentScenarios, elementsAreAllEqual());
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
        assertThat(instancesFromDifferentScenarios, elementsAreAllUnique());
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
