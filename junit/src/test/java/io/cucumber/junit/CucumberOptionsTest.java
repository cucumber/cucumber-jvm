package io.cucumber.junit;


import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.internal.runners.TestClass;

import io.cucumber.core.backend.ObjectFactory;


public final class CucumberOptionsTest {

    @Test
    public void testCucumberOptionsObjectFactoryWhenNotSet() {
        CucumberOptions options = new EmptyCucumberOptions().getOptions();
        assertNotNull(options);
        assertEquals(NoObjectFactory.class, options.objectFactory());
    }

    @Test
    public void testCucumberOptionsObjectFactory() {
        CucumberOptions options = new ObjectFactoryCucumberOptions().getOptions();
        assertNotNull(options);
        assertEquals(TestObjectFactory.class, options.objectFactory());
    }

    private interface TestObjectFactory extends ObjectFactory {}

    @CucumberOptions(objectFactory = TestObjectFactory.class)
    private static final class ObjectFactoryCucumberOptions extends CucumberOptionsProvider{}

    @CucumberOptions
    private static final class EmptyCucumberOptions extends CucumberOptionsProvider{}

    private static class CucumberOptionsProvider {
        private final CucumberOptions options_;
        CucumberOptionsProvider() {
            CucumberOptions[] cucumberOptions = getClass().getAnnotationsByType(CucumberOptions.class);
            if (cucumberOptions.length < 1) {
                fail("Annotation is missing");
                options_ = null;
            }
            else {
                options_ = cucumberOptions[0];
            }
        }
        
        CucumberOptions getOptions() {
            return options_;
        }
    }
}
