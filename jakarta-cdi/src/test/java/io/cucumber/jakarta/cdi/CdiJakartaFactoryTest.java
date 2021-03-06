package io.cucumber.jakarta.cdi;

import io.cucumber.core.backend.ObjectFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CdiJakartaFactoryTest {

    final ObjectFactory factory = new CdiJakartaFactory();

    @AfterEach
    void stop() {
        factory.stop();
        IgnoreLocalBeansXmlClassLoader.restoreClassLoader();
    }

    @Test
    void lifecycleIsIdempotent() {
        assertDoesNotThrow(factory::stop);
        factory.start();
        assertDoesNotThrow(factory::start);
        factory.stop();
        assertDoesNotThrow(factory::stop);
    }

    @Vetoed
    static class VetoedBean {

    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldCreateNewInstancesForEachScenario(boolean ignoreLocalBeansXml) {
        IgnoreLocalBeansXmlClassLoader.setClassLoader(ignoreLocalBeansXml);
        // Scenario 1
        factory.start();
        factory.addClass(VetoedBean.class);
        VetoedBean a1 = factory.getInstance(VetoedBean.class);
        VetoedBean a2 = factory.getInstance(VetoedBean.class);
        assertThat(a1, is(equalTo(a2)));
        factory.stop();

        // Scenario 2
        factory.start();
        VetoedBean b1 = factory.getInstance(VetoedBean.class);
        factory.stop();

        // VetoedBean makes it possible to compare the object outside the
        // scenario/application scope
        assertAll(
            () -> assertThat(a1, is(notNullValue())),
            () -> assertThat(a1, is(not(equalTo(b1)))),
            () -> assertThat(b1, is(not(equalTo(a1)))));
    }

    @ApplicationScoped
    static class ApplicationScopedBean {

    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldCreateApplicationScopedInstance(boolean ignoreLocalBeansXml) {
        IgnoreLocalBeansXmlClassLoader.setClassLoader(ignoreLocalBeansXml);
        factory.addClass(ApplicationScopedBean.class);
        factory.start();
        ApplicationScopedBean bean = factory.getInstance(ApplicationScopedBean.class);
        assertAll(
            // assert that it is is a CDI proxy
            () -> assertThat(bean.getClass(), not(is(ApplicationScopedBean.class))),
            () -> assertThat(bean.getClass().getSuperclass(), is(ApplicationScopedBean.class)));
        factory.stop();
    }

    static class UnmanagedBean {

    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldCreateUnmanagedInstance(boolean ignoreLocalBeansXml) {
        IgnoreLocalBeansXmlClassLoader.setClassLoader(ignoreLocalBeansXml);
        factory.start();
        UnmanagedBean bean = factory.getInstance(UnmanagedBean.class);
        assertThat(bean.getClass(), is(UnmanagedBean.class));
        factory.stop();
    }

    static class OtherStepDefinitions {

    }

    static class StepDefinitions {

        @Inject
        OtherStepDefinitions injected;

    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldInjectStepDefinitions(boolean ignoreLocalBeansXml) {
        IgnoreLocalBeansXmlClassLoader.setClassLoader(ignoreLocalBeansXml);
        factory.addClass(OtherStepDefinitions.class);
        factory.addClass(StepDefinitions.class);
        factory.start();
        StepDefinitions stepDefinitions = factory.getInstance(StepDefinitions.class);
        assertThat(stepDefinitions.injected, is(notNullValue()));
        factory.stop();
    }

    static class ParameterizedBean<K, V> {

    }

    static class ParameterizedStepDefinitions {

        @Inject
        ParameterizedBean<String, String> injected;

    }

    @Test
    void canInjectParameterizedBeansWithBeanXml() {
        factory.addClass(ParameterizedStepDefinitions.class);
        factory.start();
        ParameterizedStepDefinitions stepDefinitions = factory.getInstance(ParameterizedStepDefinitions.class);
        assertThat(stepDefinitions.injected, is(notNullValue()));
        factory.stop();
    }

    @Test
    @EnabledIf("io.cucumber.jakarta.cdi.CdiJakartaFactoryTest#usingOpenWebBeans")
    void openWebBeansCanNotInjectParameterizedBeansWithoutBeansXml() {
        IgnoreLocalBeansXmlClassLoader.setClassLoader(true);
        factory.addClass(ParameterizedStepDefinitions.class);
        factory.start();
        assertThrows(UnsatisfiedResolutionException.class,
            () -> factory.getInstance(ParameterizedStepDefinitions.class));
    }

    @Test
    @EnabledIf("io.cucumber.jakarta.cdi.CdiJakartaFactoryTest#usingWeld")
    void weldCanNotInjectParameterizedBeansWithoutBeanXml() {
        IgnoreLocalBeansXmlClassLoader.setClassLoader(true);
        factory.addClass(ParameterizedStepDefinitions.class);
        assertThrows(DeploymentException.class, factory::start);
    }

    static boolean usingWeld() {
        String name = SeContainerInitializer.newInstance().getClass().getName();
        return "org.jboss.weld.environment.se.Weld".equals(name);
    }

    static boolean usingOpenWebBeans() {
        String name = SeContainerInitializer.newInstance().getClass().getName();
        return "org.apache.openwebbeans.se.SeInitializerFacade".equals(name);
    }
}
