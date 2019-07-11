package io.cucumber.junit;

import static org.junit.Assert.*;

import org.junit.Test;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;

public final class JUnitCucumberOptionsProviderTest {

    @Test
    public void testObjectFactoryWhenNotSet() {
        CucumberOptionsAnnotationParser.CucumberOptions options = new JUnitCucumberOptionsProvider().getOptions(EmptyCucumberOptions.class);
        assertNotNull(options);
        assertNull( options.objectFactory());
    }

    @Test
    public void testObjectFactory() {
        CucumberOptionsAnnotationParser.CucumberOptions options = new JUnitCucumberOptionsProvider().getOptions(ObjectFactoryCucumberOptions.class);
        assertEquals(TestObjectFactory.class, options.objectFactory());
    }

    private interface TestObjectFactory extends ObjectFactory {}

    @CucumberOptions(objectFactory = TestObjectFactory.class)
    private static final class ObjectFactoryCucumberOptions {}

    @CucumberOptions
    private static final class EmptyCucumberOptions {}

}
