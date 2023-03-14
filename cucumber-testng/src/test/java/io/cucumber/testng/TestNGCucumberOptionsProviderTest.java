package io.cucumber.testng;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

final class TestNGCucumberOptionsProviderTest {

    private TestNGCucumberOptionsProvider optionsProvider;

    @BeforeTest
    void setUp() {
        this.optionsProvider = new TestNGCucumberOptionsProvider();
    }

    @Test
    void testObjectFactoryWhenNotSpecified() {
        io.cucumber.core.options.CucumberOptionsAnnotationParser.CucumberOptions options = this.optionsProvider
                .getOptions(ClassWithDefault.class);
        assertNotNull(options);
        assertNull(options.objectFactory());
    }

    @Test
    void testObjectFactory() {
        io.cucumber.core.options.CucumberOptionsAnnotationParser.CucumberOptions options = this.optionsProvider
                .getOptions(ClassWithCustomObjectFactory.class);
        assertNotNull(options);
        assertEquals(TestObjectFactory.class, options.objectFactory());
    }

    @Test
    void testUuidGeneratorWhenNotSpecified() {
        io.cucumber.core.options.CucumberOptionsAnnotationParser.CucumberOptions options = this.optionsProvider
                .getOptions(ClassWithDefault.class);
        assertNotNull(options);
        assertNull(options.uuidGenerator());
    }

    @Test
    void testUuidGenerator() {
        io.cucumber.core.options.CucumberOptionsAnnotationParser.CucumberOptions options = this.optionsProvider
                .getOptions(ClassWithCustomUuidGenerator.class);
        assertNotNull(options);
        assertEquals(IncrementingUuidGenerator.class, options.uuidGenerator());
    }

    @CucumberOptions()
    private static final class ClassWithDefault {

    }

    @CucumberOptions(objectFactory = TestObjectFactory.class)
    private static final class ClassWithCustomObjectFactory {

    }

    @CucumberOptions(uuidGenerator = IncrementingUuidGenerator.class)
    private static final class ClassWithCustomUuidGenerator {

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
