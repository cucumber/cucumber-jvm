package io.cucumber.needle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CreateInstanceByDefaultConstructorTest {

    public static class HasDefaultConstructor {
        // empty
    }

    public static class DoesNotHaveDefaultConstructor {
        public DoesNotHaveDefaultConstructor(final String name) {
            // empty
        }
    }

    private final CreateInstanceByDefaultConstructor createInstanceByDefaultConstructor = CreateInstanceByDefaultConstructor.INSTANCE;

    @Test
    public void shouldCreateNewInstance() {
        assertNotNull(createInstanceByDefaultConstructor.apply(HasDefaultConstructor.class));
    }

    @Test
    public void shouldNotCreateNewInstanceWhenConstructorIsMissing() {
        final Executable testMethod = () -> createInstanceByDefaultConstructor.apply(DoesNotHaveDefaultConstructor.class);
        final IllegalStateException expectedThrown = assertThrows(IllegalStateException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("Can not instantiate Instance by Default Constructor.")));
    }

}
