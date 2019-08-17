package io.cucumber.core.options;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.io.MultiLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class RuntimeOptionsParserTest {

    private RuntimeOptionsParser parser;

    @BeforeEach
    void setUp() {
        this.parser = new RuntimeOptionsParser(new RerunLoader(new MultiLoader(this.getClass().getClassLoader())));
    }

    @Test
    void testParseWithObjectFactoryArgument() {
        RuntimeOptionsBuilder optionsBuilder = this.parser.parse(Arrays.asList("--object-factory", TestObjectFactory.class.getName()));
        assertNotNull(optionsBuilder);
        RuntimeOptions options = optionsBuilder.build();
        assertNotNull(options);
        assertThat(options.getObjectFactoryClass(), is(equalTo(TestObjectFactory.class)));
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
