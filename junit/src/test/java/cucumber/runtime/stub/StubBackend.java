package cucumber.runtime.stub;

import cucumber.runtime.StepDefinition;
import io.cucumber.stepexpression.Argument;
import io.cucumber.stepexpression.TypeRegistry;
import cucumber.runtime.Backend;
import cucumber.runtime.Glue;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.pickles.PickleStep;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * We need an implementation of Backend to prevent Runtime from blowing up.
 */
@SuppressWarnings("unused")
public class StubBackend implements Backend {
    public StubBackend(ResourceLoader resourceLoader, TypeRegistry typeRegistry) {

    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        glue.addStepDefinition(createStepDefinition("background step"));
        glue.addStepDefinition(createStepDefinition("scenario name"));
        glue.addStepDefinition(createStepDefinition("scenario C"));
        glue.addStepDefinition(createStepDefinition("scenario D"));
        glue.addStepDefinition(createStepDefinition("scenario E"));
        glue.addStepDefinition(createStepDefinition("first step"));
        glue.addStepDefinition(createStepDefinition("second step"));
        glue.addStepDefinition(createStepDefinition("third step"));

    }

    public StepDefinition createStepDefinition(final String pattern) {
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
    public List<String> getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        return singletonList("STUB SNIPPET");
    }
}
