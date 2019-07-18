package io.cucumber.core.options;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.cucumber.core.backend.ObjectFactory;

final class CucumberPropertiesParserTest {

    private CucumberPropertiesParser parser;

    @BeforeEach
    void setUp() {
        this.parser = new CucumberPropertiesParser();
    }
    
    
    @Test
    void testParse() {
        TestObjectFactory factory = new TestObjectFactory();
        Class<? extends ObjectFactory> objectFactoryClass = this.parser.parse(factory.getClass().getName());
        assertEquals(factory.getClass(), objectFactoryClass);
    }

    public static final class TestObjectFactory implements ObjectFactory {

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
