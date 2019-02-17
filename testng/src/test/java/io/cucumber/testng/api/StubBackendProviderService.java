package io.cucumber.testng.api;


import gherkin.pickles.PickleStep;
import io.cucumber.core.api.options.SnippetType;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.TypeRegistry;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

public class StubBackendProviderService implements BackendProviderService {

    @Override
    public Backend create(ObjectFactory objectFactory, ResourceLoader resourceLoader, TypeRegistry typeRegistry) {
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

                @Override
                public boolean isScenarioScoped() {
                    return false;
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
        public List<String> getSnippet(PickleStep step, String keyword, SnippetType.FunctionNameGenerator functionNameGenerator) {
            return singletonList("STUB SNIPPET");
        }
    }
}
