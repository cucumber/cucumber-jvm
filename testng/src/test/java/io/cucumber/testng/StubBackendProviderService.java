package io.cucumber.testng;


import gherkin.pickles.PickleStep;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.snippets.Snippet;
import io.cucumber.core.stepexpression.Argument;

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
            glue.addStepDefinition(t -> createStepDefinition("background step"));
            glue.addStepDefinition(t -> createStepDefinition("scenario name"));
            glue.addStepDefinition(t -> createStepDefinition("scenario C"));
            glue.addStepDefinition(t -> createStepDefinition("scenario D"));
            glue.addStepDefinition(t -> createStepDefinition("scenario E"));
            glue.addStepDefinition(t -> createStepDefinition("first step"));
            glue.addStepDefinition(t -> createStepDefinition("second step"));
            glue.addStepDefinition(t -> createStepDefinition("third step"));

        }

        private StepDefinition createStepDefinition(final String pattern) {
            return new StepDefinition() {
                @Override
                public List<Argument> matchedArguments(PickleStep step) {
                    return pattern.equals(step.getText()) ? Collections.<Argument>emptyList() : null;
                }

                @Override
                public String getLocation(boolean detail) {
                    return null;
                }

                @Override
                public Integer getParameterCount() {
                    return 0;
                }

                @Override
                public void execute(Object[] args) {

                }

                @Override
                public boolean isDefinedAt(StackTraceElement stackTraceElement) {
                    return false;
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
