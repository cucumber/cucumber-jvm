package io.cucumber.core.options;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.feature.RerunLoader;
import io.cucumber.core.io.MultiLoader;

final class RuntimeOptionsParserTest {

    private RuntimeOptionsParser parser;

    @BeforeEach
    void setUp() throws Exception {
        this.parser = new RuntimeOptionsParser(new RerunLoader( new MultiLoader(this.getClass().getClassLoader())));
    }

    @Test
    void testParseWithObjectFactoryArgument() {
        RuntimeOptionsBuilder optionsBuilder = this.parser.parse(Arrays.asList("--object-factory", TestObjectFactory.class.getName()));
        assertNotNull(optionsBuilder);
        RuntimeOptions options = optionsBuilder.build();
        assertNotNull(options);
        assertEquals(TestObjectFactory.class, options.getObjectFactoryClass());
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
        public void start() {}

        @Override
        public void stop() {}
        
    }
}
