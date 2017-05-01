package cucumber.runtime.java.hk2;

import cucumber.api.java.ObjectFactory;
import org.glassfish.hk2.api.MultiException;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class Hk2FactoryTest {
    @Test
    public void constructorInjection() {
        ObjectFactory factory = new Hk2Factory();
        factory.addClass(ConstructorBellyStepdefs.class);

        // Scenario 1
        factory.start();
        ConstructorBellyStepdefs o1 = factory.getInstance(ConstructorBellyStepdefs.class);
        factory.stop();

        assertNotNull(o1);
    }

    @Test
    public void fieldInjection() {
        ObjectFactory factory = new Hk2Factory();
        factory.addClass(FieldBellyStepdefs.class);

        // Scenario 1
        factory.start();
        FieldBellyStepdefs o1 = factory.getInstance(FieldBellyStepdefs.class);
        factory.stop();

        assertNotNull(o1);
    }

    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new Hk2Factory();
        factory.addClass(ConstructorBellyStepdefs.class);

        // Scenario 1
        factory.start();
        ConstructorBellyStepdefs o1 = factory.getInstance(ConstructorBellyStepdefs.class);
        factory.stop();

        // Scenario 2
        factory.start();
        ConstructorBellyStepdefs o2 = factory.getInstance(ConstructorBellyStepdefs.class);
        factory.stop();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }

    @Test
    public void testMetaInfDefaultInhabitorPopulateTrue() {
        ObjectFactory factory = new Hk2Factory();
        factory.addClass(InhabitantBellyStepdefs.class);

        factory.start();
        InhabitantBellyStepdefs o1 = factory.getInstance(InhabitantBellyStepdefs.class);
        factory.stop();

        assertNotNull(o1);
    }


    @Test(expected = MultiException.class)
    public void testMetaInfDefaultInhabitorPopulateFalse() {
        ObjectFactory factory = new Hk2Factory();
        factory.addClass(InhabitantFailureBellyStepdefs.class);

        factory.start();
        try {
            // this should throw a MultiException because it cannot instantiate the object
            InhabitantFailureBellyStepdefs o1 = factory.getInstance(InhabitantFailureBellyStepdefs.class);
        } finally {
            factory.stop();
        }
    }

    @Test
    public void testBinder() {
        ObjectFactory factory = new Hk2Factory();
        factory.addClass(BinderBellyStepdefs.class);

        factory.start();
        BinderBellyStepdefs o1 = factory.getInstance(BinderBellyStepdefs.class);
        factory.stop();

        assertNotNull(o1);
    }

}
