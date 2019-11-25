package io.cucumber.testng;


import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.backend.StepDefinition;

import java.lang.reflect.Type;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StubBackendProviderService implements BackendProviderService {

    @Override
    public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoader) {
        return new StubBackend();
    }

    /**
     * We need an implementation of Backend to prevent Runtime from blowing up.
     */
    private static class StubBackend implements Backend {
        StubBackend() {

        }

        @Override
        public void loadGlue(Glue glue, List<URI> gluePaths) {
            glue.addStepDefinition(createStepDefinition("step"));
            glue.addStepDefinition(createStepDefinition("another step"));
            glue.addStepDefinition(createStepDefinition("foo"));
            glue.addStepDefinition(createStepDefinition("bar"));
            glue.addStepDefinition(createStepDefinition("baz"));
            glue.addStepDefinition(createStepDefinition("G&A"));
            glue.addStepDefinition(createStepDefinition("G<A"));
            glue.addStepDefinition(createStepDefinition("T>A"));

        }

        private StepDefinition createStepDefinition(final String pattern) {
            return new StepDefinition() {

                @Override
                public String getLocation() {
                    return null;
                }

                @Override
                public void execute(Object[] args) {

                }

                @Override
                public boolean isDefinedAt(StackTraceElement stackTraceElement) {
                    return false;
                }

                @Override
                public List<ParameterInfo> parameterInfos() {
                    return Collections.emptyList();
                }

                @Override
                public String getPattern() {
                    return pattern;
                }
            };
        }

        @Override
        public void buildWorld() {
        }

        @Override
        public void disposeWorld() {
        }

        @Override
        public Snippet getSnippet() {
            return new Snippet() {

                private int i = 1;

                @Override
                public MessageFormat template() {
                    return new MessageFormat("stub snippet" + i++);
                }

                @Override
                public String tableHint() {
                    return "";
                }

                @Override
                public String arguments(Map<String, Type> arguments) {
                    return "";
                }

                @Override
                public String escapePattern(String pattern) {
                    return "";
                }
            };
        }
    }
}
