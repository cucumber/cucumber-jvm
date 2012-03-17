package cucumber.runtime.java.spring;

import cucumber.runtime.java.ObjectFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class SpringFactoryTest {

    @Test
    public void shouldGiveUsNewStepInstancesForEachScenario() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(BellyStepdefs.class);

        // Scenario 1
        factory.createInstances();
        final BellyStepdefs o1 = factory.getInstance(BellyStepdefs.class);
        factory.disposeInstances();

        // Scenario 2
        factory.createInstances();
        final BellyStepdefs o2 = factory.getInstance(BellyStepdefs.class);
        factory.disposeInstances();

        assertNotNull(o1);
        assertNotNull(o2);
        assertNotSame(o1, o2);
    }

    @Test
    public void shouldNeverCreateNewApplicationBeanInstances() {
        // Feature 1
        final ObjectFactory factory1 = new SpringFactory();
        factory1.createInstances();
        final DummyComponent o1 = factory1.getInstance(DummyComponent.class);
        factory1.disposeInstances();

        // Feature 2
        final ObjectFactory factory2 = new SpringFactory();
        factory2.createInstances();
        final DummyComponent o2 = factory2.getInstance(DummyComponent.class);
        factory2.disposeInstances();

        assertNotNull(o1);
        assertNotNull(o2);
        assertSame(o1, o2);
    }

    @Test
    public void shouldRespectCommonAnnotationsInStepDefs() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithSpringAnnotations.class);
        factory.createInstances();
        WithSpringAnnotations stepdef = factory.getInstance(WithSpringAnnotations.class);
        factory.disposeInstances();

        assertNotNull(stepdef);
        assertTrue(stepdef.isAutowired());
        assertTrue(stepdef.isPostConstructCalled());
        assertTrue(stepdef.isPreDestroyCalled());
    }

    @Test
    public void shouldRespectCustomPropertyPlaceholderConfigurer() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(WithSpringAnnotations.class);
        factory.createInstances();
        WithSpringAnnotations stepdef = factory.getInstance(WithSpringAnnotations.class);
        factory.disposeInstances();

        assertEquals("property value", stepdef.getProperty());
    }
}
