package io.cucumber.core.backend;

import io.cucumber.core.backend.ObjectFactoryServiceLoader.DefaultJavaObjectFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotSame;

public class JavaObjectFactoryTest {

    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new DefaultJavaObjectFactory();
        factory.addClass(SteDef.class);

        // Scenario 1
        factory.start();
        SteDef o1 = factory.getInstance(SteDef.class);
        factory.stop();

        // Scenario 2
        factory.start();
        SteDef o2 = factory.getInstance(SteDef.class);
        factory.stop();

        assertThat(o1, is(notNullValue()));
        assertNotSame(o1, o2);
    }

    public static class SteDef {
        // we just test the instances
    }

}
