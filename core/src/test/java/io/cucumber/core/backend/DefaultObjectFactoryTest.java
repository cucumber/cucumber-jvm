package io.cucumber.core.backend;

import io.cucumber.core.exception.CucumberException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultObjectFactoryTest {
    final ObjectFactory factory = new DefaultObjectFactory();

    @Test
    void shouldGiveUsNewInstancesForEachScenario() {
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

    @Test
    void shouldThrowForNonZeroArgPublicConstructors() {
        CucumberException exception = assertThrows(CucumberException.class,
            () -> factory.getInstance(NoAccessibleConstructor.class));

        assertThat(exception.getMessage(), is("" +
                "class io.cucumber.core.backend.DefaultObjectFactoryTest$NoAccessibleConstructor does not have a public zero-argument constructor.\n"
                +
                "\n" +
                "To use dependency injection add an other ObjectFactory implementation such as:\n" +
                " * cucumber-picocontainer\n" +
                " * cucumber-spring\n" +
                " * cucumber-jakarta-cdi\n" +
                " * ...ect\n"));
    }

    public static class StepDefinition {
        // we just test the instances
    }

    public static class NoAccessibleConstructor {
        private NoAccessibleConstructor() {

        }

    }

}
