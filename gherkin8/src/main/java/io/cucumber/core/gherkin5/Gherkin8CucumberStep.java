package io.cucumber.core.gherkin5;

import io.cucumber.core.gherkin.Argument;
import io.cucumber.core.gherkin.CucumberStep;
import io.cucumber.core.gherkin.StepType;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;
import io.cucumber.messages.Messages.Pickle.PickleStep;
import io.cucumber.messages.Messages.PickleStepArgument;
import io.cucumber.messages.Messages.PickleStepArgument.PickleDocString;
import io.cucumber.messages.Messages.PickleStepArgument.PickleTable;

import java.util.stream.Stream;

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

    private static Stream<? extends Step> extractChildren(FeatureChild featureChild) {
        if (featureChild.hasScenario()) {
            return featureChild.getScenario().getStepsList().stream();
        }
        if (featureChild.hasBackground()) {
            return featureChild.getBackground().getStepsList().stream();
        }
        if (featureChild.hasRule()) {
            return featureChild.getRule().getChildrenList().stream()
                .flatMap(ruleChild -> {
                    if (ruleChild.hasScenario()) {
                        return ruleChild.getScenario().getStepsList().stream();
                    }
                    if (ruleChild.hasBackground()) {
                        return ruleChild.getBackground().getStepsList().stream();
                    }
                    return Stream.empty();
                });
        }

        return Stream.empty();
    }

    private String extractKeyWord(GherkinDocument document) {
        return document.getFeature().getChildrenList().stream()
            .flatMap(Gherkin8CucumberStep::extractChildren)
            .filter(step -> step.getLocation().getLine() == getStepLine())
            .findFirst()
            .map(Step::getKeyword)
            .orElseThrow(() -> new IllegalStateException("GherkinDocument did not contain PickleStep"));
    }

    private StepType extractKeyWordType(String keyWord, GherkinDialect dialect) {
        if (StepType.isAstrix(keyWord)) {
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
            return new Gherkin8DocStringArgument(docString);
        }
        if (argument.hasDataTable()) {
            PickleTable table = argument.getDataTable();
            return new Gherkin8DataTableArgument(table);
        }
        return null;
    }

    @Override
    public int getStepLine() {
        return step.getLocationsList().get(0).getLine();
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
