package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

class AbstractGlueDefinitionTest {

    private final Lookup lookup = new Lookup() {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getInstance(Class<T> glueClass) {
            return (T) AbstractGlueDefinitionTest.this;
        }
    };

    @Test
    void test() throws NoSuchMethodException {
        Method method = AbstractGlueDefinitionTest.class.getMethod("method");

        AbstractGlueDefinition definition = new AbstractGlueDefinition(method, lookup) {
        };

        assertThat(definition.getLocation(), startsWith("io.cucumber.java.AbstractGlueDefinitionTest.method()"));
    }

    public void method() {

    }

}
