package io.cucumber.spring;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.logging.LogRecordListener;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.java.Before;
import io.cucumber.spring.beans.BellyBean;
import io.cucumber.spring.componentannotation.WithComponentAnnotation;
import io.cucumber.spring.componentannotation.WithControllerAnnotation;
import io.cucumber.spring.contextconfig.BellyStepdefs;
import io.cucumber.spring.contextconfig.WithSpringAnnotations;
import io.cucumber.spring.contexthierarchyconfig.WithContextHierarchyAnnotation;
import io.cucumber.spring.deprecatedglue.StepDefsWithConstructorArgs;
import io.cucumber.spring.deprecatedglue.UnusedGlue;
import io.cucumber.spring.dirtiescontextconfig.DirtiesContextBellyStepDefs;
import io.cucumber.spring.metaconfig.dirties.DirtiesContextBellyMetaStepDefs;
import io.cucumber.spring.metaconfig.general.BellyMetaStepdefs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringFactoryTest {

    private LogRecordListener logRecordListener;

    @Before
    void setup() {
        logRecordListener = new LogRecordListener();
        LoggerFactory.addListener(logRecordListener);
    }

    @Test
    void shouldGiveUsNewStepInstancesForEachScenario() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(BellyStepdefs.class);

        // Scenario 1
        factory.start();
        final BellyStepdefs o1 = factory.getInstance(BellyStepdefs.class);
        factory.stop();

        // Scenario 2
        factory.start();
        final BellyStepdefs o2 = factory.getInstance(BellyStepdefs.class);
        factory.stop();

        assertNotNull(o1);
        assertNotNull(o2);
        assertNotSame(o1, o2);
    }

    @Test
    void shouldNeverCreateNewApplicationBeanInstances() {
        // Feature 1
        final ObjectFactory factory1 = new SpringFactory();
        factory1.addClass(BellyStepdefs.class);
        factory1.start();
        final BellyBean o1 = factory1.getInstance(BellyStepdefs.class).getBellyBean();
        factory1.stop();

        // Feature 2
        final ObjectFactory factory2 = new SpringFactory();
        factory2.addClass(BellyStepdefs.class);
        factory2.start();
        final BellyBean o2 = factory2.getInstance(BellyStepdefs.class).getBellyBean();
        factory2.stop();

        assertNotNull(o1);
        assertNotNull(o2);
        assertSame(o1, o2);
    }

    @Test
    void shouldNeverCreateNewApplicationBeanInstancesUsingMetaConfiguration() {
        // Feature 1
        final ObjectFactory factory1 = new SpringFactory();
        factory1.addClass(BellyMetaStepdefs.class);
        factory1.start();
        final BellyBean o1 = factory1.getInstance(BellyMetaStepdefs.class).getBellyBean();
        factory1.stop();

        // Feature 2
        final ObjectFactory factory2 = new SpringFactory();
        factory2.addClass(BellyMetaStepdefs.class);
        factory2.start();
        final BellyBean o2 = factory2.getInstance(BellyMetaStepdefs.class).getBellyBean();
        factory2.stop();

        assertNotNull(o1);
        assertNotNull(o2);
        assertSame(o1, o2);
    }

    @Test
    void shouldRespectCommonAnnotationsInStepDefs() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithSpringAnnotations.class);
        factory.start();
        WithSpringAnnotations stepdef = factory.getInstance(WithSpringAnnotations.class);
        factory.stop();

        assertNotNull(stepdef);
        assertTrue(stepdef.isAutowired());
    }

    @Test
    void shouldRespectContextHierarchyInStepDefs() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithContextHierarchyAnnotation.class);
        factory.start();
        WithContextHierarchyAnnotation stepdef = factory.getInstance(WithContextHierarchyAnnotation.class);
        factory.stop();

        assertNotNull(stepdef);
        assertTrue(stepdef.isAutowired());
    }

    @Test
    void shouldRespectDirtiesContextAnnotationsInStepDefs() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(DirtiesContextBellyStepDefs.class);

        // Scenario 1
        factory.start();
        final BellyBean o1 = factory.getInstance(DirtiesContextBellyStepDefs.class).getBellyBean();

        factory.stop();

        // Scenario 2
        factory.start();
        final BellyBean o2 = factory.getInstance(DirtiesContextBellyStepDefs.class).getBellyBean();
        factory.stop();

        assertNotNull(o1);
        assertNotNull(o2);
        assertNotSame(o1, o2);
    }

    @Test
    void shouldRespectDirtiesContextAnnotationsInStepDefsUsingMetaConfiguration() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(DirtiesContextBellyMetaStepDefs.class);

        // Scenario 1
        factory.start();
        final BellyBean o1 = factory.getInstance(DirtiesContextBellyMetaStepDefs.class).getBellyBean();

        factory.stop();

        // Scenario 2
        factory.start();
        final BellyBean o2 = factory.getInstance(DirtiesContextBellyMetaStepDefs.class).getBellyBean();
        factory.stop();

        assertNotNull(o1);
        assertNotNull(o2);
        assertNotSame(o1, o2);
    }

    @Test
    void shouldRespectCustomPropertyPlaceholderConfigurer() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithSpringAnnotations.class);
        factory.start();
        WithSpringAnnotations stepdef = factory.getInstance(WithSpringAnnotations.class);
        factory.stop();

        assertEquals("property value", stepdef.getProperty());
    }

    @Test
    void shouldFailIfMultipleClassesWithSpringAnnotationsAreFound() {
        final ObjectFactory factory = new SpringFactory();
        CucumberException actual = assertThrows(CucumberException.class, () -> {
            factory.addClass(WithSpringAnnotations.class);
            factory.addClass(BellyStepdefs.class);
        });
        assertTrue(actual.getMessage().contains("Glue class class io.cucumber.spring.contextconfig.BellyStepdefs and class io.cucumber.spring.contextconfig.WithSpringAnnotations both attempt to configure the spring context. Please ensure only one glue class configures the spring context"));
    }

    @Test
    void shouldFailIfClassWithSpringComponentAnnotationsIsFound() {
        final ObjectFactory factory = new SpringFactory();
        CucumberBackendException actual = assertThrows(CucumberBackendException.class, () -> factory.addClass(WithComponentAnnotation.class));
        assertTrue(actual.getMessage().contains("Glue class io.cucumber.spring.componentannotation.WithComponentAnnotation was annotated with @Component"));
        assertTrue(actual.getMessage().contains("Please remove the @Component annotation"));
    }

    @Test
    void shouldFailIfClassWithAnnotationAnnotatedWithSpringComponentAnnotationsIsFound() {
        final ObjectFactory factory = new SpringFactory();
        CucumberBackendException actual = assertThrows(CucumberBackendException.class, () -> factory.addClass(WithControllerAnnotation.class));
        assertTrue(actual.getMessage().contains("Glue class io.cucumber.spring.componentannotation.WithControllerAnnotation was annotated with @Controller"));
        assertTrue(actual.getMessage().contains("Please remove the @Controller annotation"));
    }

    @Test
    void shouldThrowIfStepDefClassHasNotExactlyOnePublicZeroArgumentConstructor(){
        final ObjectFactory factory = new SpringFactory();
        CucumberException actual = assertThrows(CucumberException.class, () -> factory.addClass(StepDefsWithConstructorArgs.class));
        assertEquals("Step definition class 'io.cucumber.spring.deprecatedglue.StepDefsWithConstructorArgs' should have exactly one public zero-argument constructor.", actual.getMessage());
    }

    @Test
    void shouldThrowIfStepDefClassCanNotBeInstantiated(){
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(BellyStepdefs.class);
        factory.addClass(UnusedGlue.class);
        CucumberException actual = assertThrows(CucumberException.class, factory::start);
        assertEquals("Failed to start cucumber-spring factory", actual.getMessage());
    }
}
