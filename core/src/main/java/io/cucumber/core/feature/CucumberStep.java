package io.cucumber.core.feature;

import gherkin.ast.GherkinDocument;
import gherkin.ast.Step;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;

public final class CucumberStep implements io.cucumber.core.event.CucumberStep {

    private final PickleStep pickleStep;
    private final String keyWord;
    private final Argument argument;

    CucumberStep(PickleStep pickleStep, GherkinDocument gherkinDocument) {
        this.pickleStep = pickleStep;
        this.keyWord = gherkinDocument.getFeature().getChildren().stream()
            .flatMap(scenarioDefinition -> scenarioDefinition.getSteps().stream())
            .filter(step -> step.getLocation().getLine() == getStepLine())
            .findFirst()
            .map(Step::getKeyword)
            .orElse(null);
        this.argument = extractArgument(pickleStep);
    }

    private Argument extractArgument(PickleStep pickleStep) {
        if (pickleStep.getArgument().isEmpty()) {
            return null;
        }
        gherkin.pickles.Argument argument = pickleStep.getArgument().get(0);
        if (argument instanceof PickleString) {
            PickleString docString = (PickleString) argument;
            return new DocStringArgument(docString);
        }
        if (argument instanceof PickleTable) {
            PickleTable table = (PickleTable) argument;
            return new DataTableArgument(table);
        }
        return null;
    }

    @Override
    public int getStepLine() {
        int last = pickleStep.getLocations().size() - 1;
        return pickleStep.getLocations().get(last).getLine();
    }

    @Override
    public Argument getArgument() {
        return argument;
    }

    @Override
    public String getKeyWord() {
        return keyWord;
    }

    @Override
    public String getText() {
        return pickleStep.getText();
    }
}
