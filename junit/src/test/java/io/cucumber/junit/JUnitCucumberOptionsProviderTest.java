package io.cucumber.junit;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.gherkin.Pickle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

final class JUnitCucumberOptionsProviderTest {

    private JUnitCucumberOptionsProvider optionsProvider;

    @BeforeEach
    void setUp() {
        this.optionsProvider = new JUnitCucumberOptionsProvider();
    }

    @Test
    void testCustomPredicateWhenNotSpecified() {
        io.cucumber.core.options.CucumberOptionsAnnotationParser.CucumberOptions options = this.optionsProvider
                .getOptions(ClassWithDefault.class);
        assertNull(options.customPredicateClass());
    }

    @Test
    void testCustomPredicateFactory() {
        io.cucumber.core.options.CucumberOptionsAnnotationParser.CucumberOptions options = this.optionsProvider
                .getOptions(ClassWithCustomPredicate.class);
        assertEquals(TestCustomPredicate.class, options.customPredicateClass());
    }

    @Test
    void testObjectFactoryWhenNotSpecified() {
        io.cucumber.core.options.CucumberOptionsAnnotationParser.CucumberOptions options = this.optionsProvider
                .getOptions(ClassWithDefault.class);
        assertNull(options.objectFactory());
    }

    @Test
    void testObjectFactory() {
        io.cucumber.core.options.CucumberOptionsAnnotationParser.CucumberOptions options = this.optionsProvider
                .getOptions(ClassWithCustomObjectFactory.class);
        assertNotNull(options.objectFactory());
        assertEquals(TestObjectFactory.class, options.objectFactory());
    }

    @CucumberOptions()
    private static final class ClassWithDefault {

    }

    @CucumberOptions(customPredicateClass = TestCustomPredicate.class)
    private static final class ClassWithCustomPredicate {

    }

    @CucumberOptions(objectFactory = TestObjectFactory.class)
    private static final class ClassWithCustomObjectFactory {

    }

    private static final class TestCustomPredicate implements Predicate<Pickle> {

        @Override
        public boolean test(Pickle pickle) {
            return false;
        }
    }

    private static final class TestObjectFactory implements ObjectFactory {

        @Override
        public boolean addClass(Class<?> glueClass) {
            return false;
        }

        @Override
        public <T> T getInstance(Class<T> glueClass) {
            return null;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

    }

}
