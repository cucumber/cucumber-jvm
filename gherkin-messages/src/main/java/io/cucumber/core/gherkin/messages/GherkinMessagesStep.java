package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Argument;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.gherkin.StepType;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.messages.Messages.Pickle.PickleStep;
import io.cucumber.messages.Messages.PickleStepArgument;
import io.cucumber.messages.Messages.PickleStepArgument.PickleDocString;
import io.cucumber.messages.Messages.PickleStepArgument.PickleTable;

final class GherkinMessagesStep implements Step {

    private final PickleStep pickleStep;
    private final Argument argument;
    private final String keyWord;
    private final StepType stepType;
    private final String previousGwtKeyWord;
    private final int stepLine;

    GherkinMessagesStep(PickleStep pickleStep, GherkinDialect dialect, String previousGwtKeyWord, int stepLine, String keyword) {
        this.pickleStep = pickleStep;
        this.argument = extractArgument(pickleStep);
        this.keyWord = keyword;
        this.stepType = extractKeyWordType(keyWord, dialect);
        this.previousGwtKeyWord = previousGwtKeyWord;
        this.stepLine = stepLine;
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
            //TODO: Fix this work around
            return new GherkinMessagesDocStringArgument(docString, stepLine + 1);
        }
        if (argument.hasDataTable()) {
            PickleTable table = argument.getDataTable();
            return new GherkinMessagesDataTableArgument(table, stepLine + 1);
        }
        return null;
    }

    @Override
    public int getLine() {
        return stepLine;
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
    public StepType getType() {
        return stepType;
    }

    @Override
    public String getPreviousGivenWhenThenKeyWord() {
        return previousGwtKeyWord;
    }

    @Override
    public String getText() {
        return pickleStep.getText();
    }

    @Override
    public String getId() {
        return pickleStep.getId();
    }
}
