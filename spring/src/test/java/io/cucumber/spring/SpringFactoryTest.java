package io.cucumber.spring;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.spring.beans.Belly;
import io.cucumber.spring.beans.BellyBean;
import io.cucumber.spring.beans.GlueScopedComponent;
import io.cucumber.spring.commonglue.AutowiresThirdStepDef;
import io.cucumber.spring.commonglue.OneStepDef;
import io.cucumber.spring.commonglue.ThirdStepDef;
import io.cucumber.spring.componentannotation.WithComponentAnnotation;
import io.cucumber.spring.componentannotation.WithControllerAnnotation;
import io.cucumber.spring.contextconfig.BellyStepdefs;
import io.cucumber.spring.contextconfig.WithSpringAnnotations;
import io.cucumber.spring.contexthierarchyconfig.WithContextHierarchyAnnotation;
import io.cucumber.spring.dirtiescontextconfig.DirtiesContextBellyStepDefs;
import io.cucumber.spring.metaconfig.dirties.DirtiesContextBellyMetaStepDefs;
import io.cucumber.spring.metaconfig.general.BellyMetaStepdefs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpringFactoryTest {

    @Test
    public void shouldGiveUsNewStepInstancesForEachScenario() {
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
    public void shouldNeverCreateNewApplicationBeanInstances() {
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
    public void shouldNeverCreateNewApplicationBeanInstancesUsingMetaConfiguration() {
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
    public void shouldFindStepDefsCreatedImplicitlyForAutowiring() {
        final ObjectFactory factory1 = new SpringFactory();
        factory1.addClass(WithSpringAnnotations.class);
        factory1.addClass(OneStepDef.class);
        factory1.addClass(ThirdStepDef.class);
        factory1.addClass(AutowiresThirdStepDef.class);
        factory1.start();
        final OneStepDef o1 = factory1.getInstance(OneStepDef.class);
        final ThirdStepDef o2 = factory1.getInstance(ThirdStepDef.class);
        factory1.stop();

        assertNotNull(o1.getThirdStepDef());
        assertNotNull(o2);
        assertSame(o1.getThirdStepDef(), o2);
    }

    @Test
    public void shouldReuseStepDefsCreatedImplicitlyForAutowiring() {
        final ObjectFactory factory1 = new SpringFactory();
        factory1.addClass(WithSpringAnnotations.class);
        factory1.addClass(OneStepDef.class);
        factory1.addClass(ThirdStepDef.class);
        factory1.addClass(AutowiresThirdStepDef.class);
        factory1.start();
        final OneStepDef o1 = factory1.getInstance(OneStepDef.class);
        final AutowiresThirdStepDef o3 = factory1.getInstance(AutowiresThirdStepDef.class);
        factory1.stop();

        assertNotNull(o1.getThirdStepDef());
        assertNotNull(o3.getThirdStepDef());
        assertSame(o1.getThirdStepDef(), o3.getThirdStepDef());
    }

    @Test
    public void shouldRespectCommonAnnotationsInStepDefs() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithSpringAnnotations.class);
        factory.start();
        WithSpringAnnotations stepdef = factory.getInstance(WithSpringAnnotations.class);
        factory.stop();

        assertNotNull(stepdef);
        assertTrue(stepdef.isAutowired());
    }

    @Test
    public void shouldRespectContextHierarchyInStepDefs() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithContextHierarchyAnnotation.class);
        factory.start();
        WithContextHierarchyAnnotation stepdef = factory.getInstance(WithContextHierarchyAnnotation.class);
        factory.stop();

        assertNotNull(stepdef);
        assertTrue(stepdef.isAutowired());
    }

    @Test
    public void shouldRespectDirtiesContextAnnotationsInStepDefs() {
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
    public void shouldRespectDirtiesContextAnnotationsInStepDefsUsingMetaConfiguration() {
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
    public void shouldRespectCustomPropertyPlaceholderConfigurer() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithSpringAnnotations.class);
        factory.start();
        WithSpringAnnotations stepdef = factory.getInstance(WithSpringAnnotations.class);
        factory.stop();

        assertEquals("property value", stepdef.getProperty());
    }

    @Test
    public void shouldUseCucumberXmlIfNoClassWithSpringAnnotationIsFound() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(Object.class);
        factory.start();
        final BellyBean o1 = factory.getInstance(BellyBean.class);
        factory.stop();

        assertNotNull(o1);
    }

    @Test
    public void shouldFailIfMultipleClassesWithSpringAnnotationsAreFound() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithSpringAnnotations.class);

        final Executable testMethod = () -> factory.addClass(BellyStepdefs.class);
        final CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Glue class class io.cucumber.spring.contextconfig.BellyStepdefs and class io.cucumber.spring.contextconfig.WithSpringAnnotations both attempt to configure the spring context. Please ensure only one glue class configures the spring context"
        )));
    }

    @Test
    public void shouldFailIfClassWithSpringComponentAnnotationsIsFound() {
        final ObjectFactory factory = new SpringFactory();

        final Executable testMethod = () -> factory.addClass(WithComponentAnnotation.class);
        final CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Glue class io.cucumber.spring.componentannotation.WithComponentAnnotation was annotated with @Component; marking it as a candidate for auto-detection by Spring. Glue classes are detected and registered by Cucumber. Auto-detection of glue classes by spring may lead to duplicate bean definitions. Please remove the @Component annotation"
        )));
    }

    @Test
    public void shouldFailIfClassWithAnnotationAnnotatedWithSpringComponentAnnotationsIsFound() {
        final ObjectFactory factory = new SpringFactory();

        final Executable testMethod = () -> factory.addClass(WithControllerAnnotation.class);
        final CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "Glue class io.cucumber.spring.componentannotation.WithControllerAnnotation was annotated with @Controller; marking it as a candidate for auto-detection by Spring. Glue classes are detected and registered by Cucumber. Auto-detection of glue classes by spring may lead to duplicate bean definitions. Please remove the @Controller annotation"
        )));
    }

    @Test
    public void shouldGlueScopedSpringBeanBehaveLikeGlueLifecycle() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithSpringAnnotations.class);

        // Scenario 1
        factory.start();
        final Belly belly1 = factory.getInstance(Belly.class);
        final GlueScopedComponent glue1 = factory.getInstance(GlueScopedComponent.class);
        assertNotNull(belly1);
        assertNotNull(glue1);
        factory.stop();

        // Scenario 2
        final Belly belly2 = factory.getInstance(Belly.class);
        final GlueScopedComponent glue2 = factory.getInstance(GlueScopedComponent.class);
        assertNotNull(belly2);
        assertNotNull(glue2);
        assertNotSame(glue1, glue2);
        assertSame(belly1, belly2);
    }

}
