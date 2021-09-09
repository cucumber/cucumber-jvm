package io.cucumber.openejb;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;

class OpenEJBObjectFactoryTest {

    @Test
    void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new OpenEJBObjectFactory();
        factory.addClass(BellyStepDefinitions.class);

        // Scenario 1
        factory.start();
        BellyStepDefinitions o1 = factory.getInstance(BellyStepDefinitions.class);
        factory.stop();

        // Scenario 2
        factory.start();
        BellyStepDefinitions o2 = factory.getInstance(BellyStepDefinitions.class);
        factory.stop();

        assertThat(o1, is(notNullValue()));
        assertThat(o1, is(not(equalTo(o2))));
        assertThat(o2, is(not(equalTo(o1))));
    }

}
