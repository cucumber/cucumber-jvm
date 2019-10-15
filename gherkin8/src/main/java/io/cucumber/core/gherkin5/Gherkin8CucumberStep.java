package io.cucumber.core.gherkin5;

import io.cucumber.core.gherkin.Argument;
import io.cucumber.core.gherkin.CucumberStep;
import io.cucumber.core.gherkin.StepType;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;
import io.cucumber.messages.Messages.Pickle.PickleStep;
import io.cucumber.messages.Messages.PickleStepArgument;
import io.cucumber.messages.Messages.PickleStepArgument.PickleDocString;
import io.cucumber.messages.Messages.PickleStepArgument.PickleTable;

import java.util.Collection;

public final class Gherkin8CucumberStep implements CucumberStep {

    private final PickleStep step;
    private final Argument argument;
    private final String keyWord;
    private final StepType stepType;
    private final String previousGwtKeyWord;

    Gherkin8CucumberStep(PickleStep step, GherkinDocument document, GherkinDialect dialect, String previousGwtKeyWord) {
        this.step = step;
        this.argument = extractArgument(step);
        this.keyWord = extractKeyWord(document);
        this.stepType = extractKeyWordType(keyWord, dialect);
        this.previousGwtKeyWord = previousGwtKeyWord;
    }

    private String extractKeyWord(GherkinDocument document) {
        return document.getFeature().getChildrenList().stream()
            .map(FeatureChild::getScenario)
            .map(GherkinDocument.Feature.Scenario::getStepsList)
            .flatMap(Collection::stream)
            .filter(step -> step.getLocation().getLine() == getStepLine())
            .findFirst()
            .map(Step::getKeyword)
            .orElseThrow(() ->  new IllegalStateException("GherkinDocument did not contain PickleStep"));
    }

    private StepType extractKeyWordType(String keyWord, GherkinDialect dialect) {
        if(StepType.isAstrix(keyWord)){
            return StepType.OTHER;
        }
        if (dialect.getGivenKeywords().contains(keyWord)) {
            return StepType.GIVEN;
        }
        if (dialect.getWhenKeywords().contains(keyWord)) {
            return StepType.WHEN;
        }
        if (dialect.getThenKeywords().contains(keyWord)) {
            return StepType.THEN;
        }
        if (dialect.getAndKeywords().contains(keyWord)) {
            return StepType.AND;
        }
        if (dialect.getButKeywords().contains(keyWord)) {
            return StepType.BUT;
        }
        throw new IllegalStateException("Keyword " + keyWord + " was neither given, when, then, and, but nor *");
    }

    private Argument extractArgument(PickleStep pickleStep) {
        PickleStepArgument argument = pickleStep.getArgument();
        if (argument.hasDocString()) {
            PickleDocString docString = argument.getDocString();
            return new DocStringArgument(docString);
        }
        if (argument.hasDataTable()) {
            PickleTable table = argument.getDataTable();
            return new DataTableArgument(table);
        }
        return null;
    }

    @Override
    public int getStepLine() {
        int last = step.getLocationsList().size() - 1;
        return step.getLocationsList().get(last).getLine();
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
    public StepType getStepType() {
        return stepType;
    }

    @Override
    public String getPreviousGivenWhenThenKeyWord() {
        return previousGwtKeyWord;
    }

    @Override
    public String getText() {
        return step.getText();
    }
}
