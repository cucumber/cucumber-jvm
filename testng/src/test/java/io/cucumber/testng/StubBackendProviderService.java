package io.cucumber.testng;


import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.snippets.Snippet;

import java.lang.reflect.Type;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StubBackendProviderService implements BackendProviderService {

    @Override
    public Backend create(Lookup lookup, Container container, ResourceLoader resourceLoader) {
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
            glue.addStepDefinition(createStepDefinition("background step"));
            glue.addStepDefinition(createStepDefinition("scenario name"));
            glue.addStepDefinition(createStepDefinition("scenario C"));
            glue.addStepDefinition(createStepDefinition("scenario D"));
            glue.addStepDefinition(createStepDefinition("scenario E"));
            glue.addStepDefinition(createStepDefinition("first step"));
            glue.addStepDefinition(createStepDefinition("second step"));
            glue.addStepDefinition(createStepDefinition("third step"));

        }

        private StepDefinition createStepDefinition(final String pattern) {
            return new StepDefinition() {

                @Override
                public String getLocation(boolean detail) {
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
                @Override
                public MessageFormat template() {
                    return new MessageFormat("");
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
