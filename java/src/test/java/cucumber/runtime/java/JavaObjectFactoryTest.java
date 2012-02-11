package cucumber.runtime.java;

import cucumber.fallback.runtime.java.DefaultJavaObjectFactory;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class JavaObjectFactoryTest {
    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new DefaultJavaObjectFactory();
        factory.addClass(StepDef.class);

        // Scenario 1
        factory.createInstances();
        StepDef o1 = factory.getInstance(StepDef.class);
        factory.disposeInstances();

        // Scenario 2
        factory.createInstances();
        StepDef o2 = factory.getInstance(StepDef.class);
        factory.disposeInstances();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }

    @Test
    public void shouldInjectStepDefinition() {
        ObjectFactory factory = new DefaultJavaObjectFactory();
        factory.addClass(StepDef.class);
        factory.addClass(StepDef2.class);

        factory.createInstances();

        StepDef def = factory.getInstance(StepDef.class);
        StepDef2 def2 = factory.getInstance(StepDef2.class);

        assertSame(def, def2.getDef());
        assertSame(def2, def.getDef2());
    }

    @Test
    public void shouldNotThrowExceptionWOInjection() {
        ObjectFactory factory = new DefaultJavaObjectFactory();
        factory.addClass(StepDef.class);
        factory.addClass(StepDefEmpty.class);

        factory.createInstances();
    }


    @Test
    public void shouldNotInitiateAbstractClass() {
        ObjectFactory factory = new DefaultJavaObjectFactory();
        factory.addClass(AbstractDef.class);
        factory.addClass(StepDef.class);

        factory.createInstances();

        assertNull(factory.getInstance(AbstractDef.class));
        assertNotNull(factory.getInstance(StepDef.class));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWithoutDefaultConstructor() {
        ObjectFactory factory = new DefaultJavaObjectFactory();
        factory.addClass(StepDefNoDefaultConstructor.class);

        factory.createInstances();
    }

    public static abstract class AbstractDef {

    }

    @SuppressWarnings("unused")
    public static class StepDefNoDefaultConstructor {
        private String s;

        public StepDefNoDefaultConstructor(String s) {
            this.s = s;
        }
    }

    @SuppressWarnings("unused")
    public static class StepDef {
        // we just test the instances
        private StepDef2 def2;

        public StepDef2 getDef2() {
            return def2;
        }

        public void setDef2(StepDef2 def) {
            this.def2 = def;
        }
    }

    @SuppressWarnings("unused")
    public static class StepDef2 {
        // we just test the instances
        private StepDef def;

        public StepDef getDef() {
            return def;
        }

        public void setDef(StepDef def) {
            this.def = def;
        }
    }

    public static class StepDefEmpty {

    }
}
