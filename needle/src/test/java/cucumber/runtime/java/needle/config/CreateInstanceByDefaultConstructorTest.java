package cucumber.runtime.java.needle.config;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

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

    @Test(expected = IllegalStateException.class)
    public void shouldNotCreateNewInstanceWhenConstructorIsMissing() {
        createInstanceByDefaultConstructor.apply(DoesNotHaveDefaultConstructor.class);
    }

}
