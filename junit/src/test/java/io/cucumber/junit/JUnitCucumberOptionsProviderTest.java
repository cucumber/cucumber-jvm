package io.cucumber.junit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.cucumber.core.backend.ObjectFactory;

final class JUnitCucumberOptionsProviderTest {

    private JUnitCucumberOptionsProvider optionsProvider;

    @BeforeEach
    void setUp() {
        this.optionsProvider = new JUnitCucumberOptionsProvider();
    }

    @Test
    void testObjectFactoryWhenNotSpecified() {
        io.cucumber.core.options.CucumberOptionsAnnotationParser.CucumberOptions options = this.optionsProvider.getOptions(ClassWithDefault.class);
        assertNull(options.objectFactory());
    }

    @Test
    void testObjectFactory() {
        io.cucumber.core.options.CucumberOptionsAnnotationParser.CucumberOptions options = this.optionsProvider.getOptions(ClassWithCustomObjectFactory.class);
        assertNotNull(options.objectFactory());
        assertEquals(TestObjectFactory.class, options.objectFactory());
    }

    @CucumberOptions()
    private static final class ClassWithDefault {}

    @CucumberOptions(objectFactory = TestObjectFactory.class)
    private static final class ClassWithCustomObjectFactory {}
    
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
        public void start() {}

        @Override
        public void stop() {}
    }
}
