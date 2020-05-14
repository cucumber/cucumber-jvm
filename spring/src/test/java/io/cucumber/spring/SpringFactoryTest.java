package io.cucumber.spring;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.spring.beans.Belly;
import io.cucumber.spring.beans.BellyBean;
import io.cucumber.spring.beans.DummyComponent;
import io.cucumber.spring.beans.GlueScopedComponent;
import io.cucumber.spring.commonglue.AutowiresThirdStepDef;
import io.cucumber.spring.commonglue.OneStepDef;
import io.cucumber.spring.commonglue.ThirdStepDef;
import io.cucumber.spring.componentannotation.WithComponentAnnotation;
import io.cucumber.spring.componentannotation.WithControllerAnnotation;
import io.cucumber.spring.contextconfig.BellyStepDefinitions;
import io.cucumber.spring.contexthierarchyconfig.WithContextHierarchyAnnotation;
import io.cucumber.spring.dirtiescontextconfig.DirtiesContextBellyStepDefinitions;
import io.cucumber.spring.metaconfig.dirties.DirtiesContextBellyMetaStepDefinitions;
import io.cucumber.spring.metaconfig.general.BellyMetaStepDefinitions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringFactoryTest {

    @Test
    void shouldGiveUsNewStepInstancesForEachScenario() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(BellyStepDefinitions.class);

        // Scenario 1
        factory.start();
        final BellyStepDefinitions o1 = factory.getInstance(BellyStepDefinitions.class);
        factory.stop();

        // Scenario 2
        factory.start();
        final BellyStepDefinitions o2 = factory.getInstance(BellyStepDefinitions.class);
        factory.stop();

        assertAll(
            () -> assertThat(o1, is(notNullValue())),
            () -> assertThat(o2, is(notNullValue())),
            () -> assertThat(o1, is(not(equalTo(o2)))),
            () -> assertThat(o2, is(not(equalTo(o1)))));
    }

    @Test
    void shouldNeverCreateNewApplicationBeanInstances() {
        // Feature 1
        final ObjectFactory factory1 = new SpringFactory();
        factory1.addClass(BellyStepDefinitions.class);
        factory1.start();
        final BellyBean o1 = factory1.getInstance(BellyStepDefinitions.class).getBellyBean();
        factory1.stop();

        // Feature 2
        final ObjectFactory factory2 = new SpringFactory();
        factory2.addClass(BellyStepDefinitions.class);
        factory2.start();
        final BellyBean o2 = factory2.getInstance(BellyStepDefinitions.class).getBellyBean();
        factory2.stop();

        assertAll(
            () -> assertThat(o1, is(notNullValue())),
            () -> assertThat(o2, is(notNullValue())),
            () -> assertThat(o1, is(equalTo(o1))),
            () -> assertThat(o2, is(equalTo(o2))));
    }

    @Test
    void shouldNeverCreateNewApplicationBeanInstancesUsingMetaConfiguration() {
        // Feature 1
        final ObjectFactory factory1 = new SpringFactory();
        factory1.addClass(BellyMetaStepDefinitions.class);
        factory1.start();
        final BellyBean o1 = factory1.getInstance(BellyMetaStepDefinitions.class).getBellyBean();
        factory1.stop();

        // Feature 2
        final ObjectFactory factory2 = new SpringFactory();
        factory2.addClass(BellyMetaStepDefinitions.class);
        factory2.start();
        final BellyBean o2 = factory2.getInstance(BellyMetaStepDefinitions.class).getBellyBean();
        factory2.stop();

        assertAll(
            () -> assertThat(o1, is(notNullValue())),
            () -> assertThat(o2, is(notNullValue())),
            () -> assertThat(o1, is(equalTo(o1))),
            () -> assertThat(o2, is(equalTo(o2))));
    }

    @Test
    void shouldFindStepDefsCreatedImplicitlyForAutowiring() {
        final ObjectFactory factory1 = new SpringFactory();
        factory1.addClass(WithSpringAnnotations.class);
        factory1.addClass(OneStepDef.class);
        factory1.addClass(ThirdStepDef.class);
        factory1.addClass(AutowiresThirdStepDef.class);
        factory1.start();
        final OneStepDef o1 = factory1.getInstance(OneStepDef.class);
        final ThirdStepDef o2 = factory1.getInstance(ThirdStepDef.class);
        factory1.stop();

        assertAll(
            () -> assertThat(o1.getThirdStepDef(), is(notNullValue())),
            () -> assertThat(o2, is(notNullValue())),
            () -> assertThat(o1.getThirdStepDef(), is(equalTo(o2))),
            () -> assertThat(o2, is(equalTo(o1.getThirdStepDef()))));
    }

    @Test
    void shouldReuseStepDefsCreatedImplicitlyForAutowiring() {
        final ObjectFactory factory1 = new SpringFactory();
        factory1.addClass(WithSpringAnnotations.class);
        factory1.addClass(OneStepDef.class);
        factory1.addClass(ThirdStepDef.class);
        factory1.addClass(AutowiresThirdStepDef.class);
        factory1.start();
        final OneStepDef o1 = factory1.getInstance(OneStepDef.class);
        final AutowiresThirdStepDef o3 = factory1.getInstance(AutowiresThirdStepDef.class);
        factory1.stop();

        assertAll(
            () -> assertThat(o1.getThirdStepDef(), is(notNullValue())),
            () -> assertThat(o3.getThirdStepDef(), is(notNullValue())),
            () -> assertThat(o1.getThirdStepDef(), is(equalTo(o3.getThirdStepDef()))),
            () -> assertThat(o3.getThirdStepDef(), is(equalTo(o1.getThirdStepDef()))));
    }

    @Test
    void shouldRespectCommonAnnotationsInStepDefs() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithSpringAnnotations.class);
        factory.start();
        WithSpringAnnotations stepdef = factory.getInstance(WithSpringAnnotations.class);
        factory.stop();

        assertThat(stepdef, is(notNullValue()));
        assertTrue(stepdef.isAutowired());
    }

    @Test
    void shouldRespectContextHierarchyInStepDefs() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithContextHierarchyAnnotation.class);
        factory.start();
        WithContextHierarchyAnnotation stepdef = factory.getInstance(WithContextHierarchyAnnotation.class);
        factory.stop();

        assertThat(stepdef, is(notNullValue()));
        assertTrue(stepdef.isAutowired());
    }

    @Test
    void shouldRespectDirtiesContextAnnotationsInStepDefs() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(DirtiesContextBellyStepDefinitions.class);

        // Scenario 1
        factory.start();
        final BellyBean o1 = factory.getInstance(DirtiesContextBellyStepDefinitions.class).getBellyBean();

        factory.stop();

        // Scenario 2
        factory.start();
        final BellyBean o2 = factory.getInstance(DirtiesContextBellyStepDefinitions.class).getBellyBean();
        factory.stop();

        assertAll(
            () -> assertThat(o1, is(notNullValue())),
            () -> assertThat(o2, is(notNullValue())),
            () -> assertThat(o1, is(not(equalTo(o2)))),
            () -> assertThat(o2, is(not(equalTo(o1)))));
    }

    @Test
    void shouldRespectDirtiesContextAnnotationsInStepDefsUsingMetaConfiguration() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(DirtiesContextBellyMetaStepDefinitions.class);

        // Scenario 1
        factory.start();
        final BellyBean o1 = factory.getInstance(DirtiesContextBellyMetaStepDefinitions.class).getBellyBean();

        factory.stop();

        // Scenario 2
        factory.start();
        final BellyBean o2 = factory.getInstance(DirtiesContextBellyMetaStepDefinitions.class).getBellyBean();
        factory.stop();

        assertAll(
            () -> assertThat(o1, is(notNullValue())),
            () -> assertThat(o2, is(notNullValue())),
            () -> assertThat(o1, is(not(equalTo(o2)))),
            () -> assertThat(o2, is(not(equalTo(o1)))));
    }

    @Test
    void shouldRespectCustomPropertyPlaceholderConfigurer() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithSpringAnnotations.class);
        factory.start();
        WithSpringAnnotations stepdef = factory.getInstance(WithSpringAnnotations.class);
        factory.stop();

        assertThat(stepdef.getProperty(), is(equalTo("property value")));
    }

    @Test
    void shouldFailIfMultipleClassesWithSpringAnnotationsAreFound() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithSpringAnnotations.class);

        Executable testMethod = () -> factory.addClass(BellyStepDefinitions.class);
        CucumberBackendException actualThrown = assertThrows(CucumberBackendException.class, testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo(
            "Glue class class io.cucumber.spring.contextconfig.BellyStepDefinitions and class io.cucumber.spring.SpringFactoryTest$WithSpringAnnotations are both annotated with @CucumberContextConfiguration.\n"
                    +
                    "Please ensure only one class configures the spring context")));
    }

    @Test
    void shouldFailIfClassWithSpringComponentAnnotationsIsFound() {
        final ObjectFactory factory = new SpringFactory();

        Executable testMethod = () -> factory.addClass(WithComponentAnnotation.class);
        CucumberBackendException actualThrown = assertThrows(CucumberBackendException.class, testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo(
            "Glue class io.cucumber.spring.componentannotation.WithComponentAnnotation was annotated with @Component; marking it as a candidate for auto-detection by Spring. Glue classes are detected and registered by Cucumber. Auto-detection of glue classes by spring may lead to duplicate bean definitions. Please remove the @Component annotation")));
    }

    @Test
    void shouldFailIfClassWithAnnotationAnnotatedWithSpringComponentAnnotationsIsFound() {
        final ObjectFactory factory = new SpringFactory();

        Executable testMethod = () -> factory.addClass(WithControllerAnnotation.class);
        CucumberBackendException actualThrown = assertThrows(CucumberBackendException.class, testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo(
            "Glue class io.cucumber.spring.componentannotation.WithControllerAnnotation was annotated with @Controller; marking it as a candidate for auto-detection by Spring. Glue classes are detected and registered by Cucumber. Auto-detection of glue classes by spring may lead to duplicate bean definitions. Please remove the @Controller annotation")));
    }

    @Test
    void shouldGlueScopedSpringBeanBehaveLikeGlueLifecycle() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithSpringAnnotations.class);

        // Scenario 1
        factory.start();
        long bellyInstance1 = factory.getInstance(Belly.class).getInstanceId();
        long glueInstance1 = factory.getInstance(GlueScopedComponent.class).getInstanceId();
        factory.stop();

        // Scenario 2
        factory.start();
        long bellyInstance2 = factory.getInstance(Belly.class).getInstanceId();
        long glueInstance2 = factory.getInstance(GlueScopedComponent.class).getInstanceId();
        factory.stop();

        assertAll(
            () -> assertThat(glueInstance1, is(not(glueInstance2))),
            () -> assertThat(glueInstance2, is(not(glueInstance1))),
            () -> assertThat(bellyInstance1, is(bellyInstance2)),
            () -> assertThat(bellyInstance2, is(bellyInstance1)));
    }

    @Test
    void shouldThrowWhenGlueScopedSpringBeanAreUsedOutsideLifecycle() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithSpringAnnotations.class);

        factory.start();
        final Belly belly = factory.getInstance(Belly.class);
        final GlueScopedComponent glue = factory.getInstance(GlueScopedComponent.class);
        factory.stop();

        assertDoesNotThrow(belly::getInstanceId);
        assertThrows(BeanCreationException.class, glue::getInstanceId);
    }

    @Test
    void shouldBeStoppableWhenFacedWithInvalidConfiguration() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithEmptySpringAnnotations.class);

        IllegalStateException exception = assertThrows(IllegalStateException.class, factory::start);
        assertThat(exception.getMessage(),
            containsString("DelegatingSmartContextLoader was unable to detect defaults"));
        assertDoesNotThrow(factory::stop);
    }

    @Test
    void shouldBeStoppableWhenFacedWithMissingContextConfiguration() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithoutContextConfiguration.class);

        IllegalStateException exception = assertThrows(IllegalStateException.class, factory::start);
        assertThat(exception.getMessage(), containsString("Failed to load ApplicationContext"));
        assertDoesNotThrow(factory::stop);
    }

    @CucumberContextConfiguration
    @ContextConfiguration("classpath:cucumber.xml")
    public static class WithSpringAnnotations {

        private boolean autowired;

        @Value("${cukes.test.property}")
        private String property;

        @Autowired
        public void setAutowiredCollaborator(DummyComponent collaborator) {
            autowired = true;
        }

        public boolean isAutowired() {
            return autowired;
        }

        public String getProperty() {
            return property;
        }

    }

    @CucumberContextConfiguration
    @ContextConfiguration()
    public static class WithEmptySpringAnnotations {

    }

    @CucumberContextConfiguration
    public static class WithoutContextConfiguration {

    }

}
