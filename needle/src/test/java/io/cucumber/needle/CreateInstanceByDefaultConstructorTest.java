package io.cucumber.needle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateInstanceByDefaultConstructorTest {

    private final CreateInstanceByDefaultConstructor createInstanceByDefaultConstructor = CreateInstanceByDefaultConstructor.INSTANCE;

    @Test
    void shouldCreateNewInstance() {
        assertThat(createInstanceByDefaultConstructor.apply(HasDefaultConstructor.class), is(notNullValue()));
    }

    @Test
    void shouldNotCreateNewInstanceWhenConstructorIsMissing() {
        Executable testMethod = () -> createInstanceByDefaultConstructor.apply(DoesNotHaveDefaultConstructor.class);
        IllegalStateException expectedThrown = assertThrows(IllegalStateException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("Can not instantiate Instance by Default Constructor.")));
    }

    public static class HasDefaultConstructor {
        // empty
    }

    public static class DoesNotHaveDefaultConstructor {

        public DoesNotHaveDefaultConstructor(final String name) {
            // empty
        }

    }

}
