package io.cucumber.core.runtime;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader.DefaultJavaObjectFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

class JavaObjectFactoryTest {

    @Test
    void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new DefaultJavaObjectFactory();
        factory.addClass(StepDefinition.class);

        // Scenario 1
        factory.start();
        StepDefinition o1 = factory.getInstance(StepDefinition.class);
        factory.stop();

        // Scenario 2
        factory.start();
        StepDefinition o2 = factory.getInstance(StepDefinition.class);
        factory.stop();

        assertAll(
            () -> assertThat(o1, is(notNullValue())),
            () -> assertThat(o1, is(not(equalTo(o2)))),
            () -> assertThat(o2, is(not(equalTo(o1)))));
    }

    public static class StepDefinition {
        // we just test the instances
    }

}
