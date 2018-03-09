package cucumber.runtime.java.spring;

import cucumber.runtime.CucumberException;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.java.spring.beans.BellyBean;
import cucumber.runtime.java.spring.commonglue.AutowiresPlatformTransactionManager;
import cucumber.runtime.java.spring.commonglue.AutowiresThirdStepDef;
import cucumber.runtime.java.spring.commonglue.OneStepDef;
import cucumber.runtime.java.spring.commonglue.ThirdStepDef;
import cucumber.runtime.java.spring.componentannotation.WithComponentAnnotation;
import cucumber.runtime.java.spring.componentannotation.WithControllerAnnotation;
import cucumber.runtime.java.spring.metaconfig.general.BellyMetaStepdefs;
import cucumber.runtime.java.spring.contextconfig.BellyStepdefs;
import cucumber.runtime.java.spring.contextconfig.WithSpringAnnotations;
import cucumber.runtime.java.spring.contexthierarchyconfig.WithContextHierarchyAnnotation;
import cucumber.runtime.java.spring.dirtiescontextconfig.DirtiesContextBellyStepDefs;
import cucumber.runtime.java.spring.metaconfig.dirties.DirtiesContextBellyMetaStepDefs;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SpringFactoryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
    public void shouldGiveUsNewInstancesOfGlueScopeClassesForEachScenario() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(BellyStepdefs.class);
        factory.addClass(AutowiresPlatformTransactionManager.class);

        // Scenario 1
        factory.start();
        final PlatformTransactionManager o1 =
                factory.getInstance(AutowiresPlatformTransactionManager.class).getTransactionManager();
        factory.stop();

        // Scenario 2
        factory.start();
        final PlatformTransactionManager o2 =
                factory.getInstance(AutowiresPlatformTransactionManager.class).getTransactionManager();
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
        factory.addClass(AutowiresPlatformTransactionManager.class);
        factory.start();
        final AutowiresPlatformTransactionManager o1 =
                factory.getInstance(AutowiresPlatformTransactionManager.class);
        factory.stop();

        assertNotNull(o1);
        assertNotNull(o1.getTransactionManager());
    }

    @Test
    public void shouldFailIfMultipleClassesWithSpringAnnotationsAreFound() {
        expectedException.expect(CucumberException.class);
        expectedException.expectMessage("Glue class class cucumber.runtime.java.spring.contextconfig.BellyStepdefs and class cucumber.runtime.java.spring.contextconfig.WithSpringAnnotations both attempt to configure the spring context");
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithSpringAnnotations.class);
        factory.addClass(BellyStepdefs.class);
    }

    @Test
    public void shouldFailIfClassWithSpringComponentAnnotationsIsFound() {
        expectedException.expect(CucumberException.class);
        expectedException.expectMessage("Glue class cucumber.runtime.java.spring.componentannotation.WithComponentAnnotation was annotated with @Component");
        expectedException.expectMessage("Please remove the @Component annotation");
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithComponentAnnotation.class);
    }

    @Test
    public void shouldFailIfClassWithAnnotationAnnotatedWithSpringComponentAnnotationsIsFound() {
        expectedException.expect(CucumberException.class);
        expectedException.expectMessage("Glue class cucumber.runtime.java.spring.componentannotation.WithControllerAnnotation was annotated with @Controller");
        expectedException.expectMessage("Please remove the @Controller annotation");
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithControllerAnnotation.class);

    }
}
