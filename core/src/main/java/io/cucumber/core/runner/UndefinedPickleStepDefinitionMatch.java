package io.cucumber.core.runner;

import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.api.Scenario;
import gherkin.pickles.PickleStep;

import java.util.Collections;

final class UndefinedPickleStepDefinitionMatch extends PickleStepDefinitionMatch {

    UndefinedPickleStepDefinitionMatch(PickleStep step) {
        super(Collections.<Argument>emptyList(), new NoStepDefinition(), null, step);
    }

    @Override
    public void runStep(Scenario scenario) {
        throw new UndefinedStepDefinitionException();
    }

    @Override
    public void dryRunStep(Scenario scenario) throws Throwable {
        runStep(scenario);
    }

}
