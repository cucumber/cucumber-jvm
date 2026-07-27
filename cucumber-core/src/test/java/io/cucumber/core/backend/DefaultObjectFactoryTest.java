package io.cucumber.core.backend;

import io.cucumber.core.exception.CucumberException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class DefaultObjectFactoryTest {
    final ObjectFactory factory = new DefaultObjectFactory();

    @Test
    void shouldCreateNewInstancesForEachScenario() {
        factory.addClass(DefaultConstructor.class);

        // Scenario 1
        factory.start();
        DefaultConstructor o1 = factory.getInstance(DefaultConstructor.class);
        factory.stop();

        // Scenario 2
        factory.start();
        DefaultConstructor o2 = factory.getInstance(DefaultConstructor.class);
        factory.stop();

        assertAll(
            () -> assertThat(o1).isNotNull(),
            () -> assertThat(o1).isNotEqualTo(o2),
            () -> assertThat(o2).isNotEqualTo(o1));
    }

    @ParameterizedTest
    @ValueSource(classes = {
            DefaultConstructor.class, PublicConstructor.class, PackagePrivateConstructor.class,
            ProtectedConstructor.class, SubclassWithDefaultConstructor.class
    })
    @ValueSource(strings = "io.cucumber.core.backend.fixtures.PackagePrivateClass")
    void shouldCreateNewInstanceUsingAccessibleConstructors(Object arg) throws ClassNotFoundException {
        Class<?> clazz;
        if (arg instanceof Class<?> instance) {
            clazz = instance;
        } else if (arg instanceof String className) {
            clazz = Class.forName(className);
        } else {
            throw new IllegalArgumentException();
        }

        factory.addClass(clazz);
        factory.start();
        Object o1 = factory.getInstance(clazz);
        factory.stop();
        assertThat(o1).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(classes = { PrivateConstructor.class, ConstructorWithArguments.class })
    void shouldThrowForInaccessibleConstructors(Class<?> clazz) {
        CucumberException exception = assertThrows(CucumberException.class, () -> factory.getInstance(clazz));
        assertThat(exception).hasMessageStartingWith(
            "class %s does not have an single accessible zero-argument constructor.".formatted(clazz.getName()));
    }

    public static final class DefaultConstructor {
        // we just test the instances
    }

    public static final class PublicConstructor {
        public PublicConstructor() {
            /* no-op */
        }
    }

    public static final class ConstructorWithArguments {
        public ConstructorWithArguments(String argument) {
            /* no-op */
        }
    }

    static final class PackagePrivateConstructor {
        PackagePrivateConstructor() {
            /* no-op */
        }
    }

    @SuppressWarnings("ProtectedMembersInFinalClass")
    protected static final class ProtectedConstructor {
        protected ProtectedConstructor() {
            /* no-op */
        }
    }

    private static final class PrivateConstructor {
        private PrivateConstructor() {
            /* no-op */
        }
    }

    static abstract class AbstractSuperClassDefaultConstructor {

    }

    static class SubclassWithDefaultConstructor extends AbstractSuperClassDefaultConstructor {

    }

}
