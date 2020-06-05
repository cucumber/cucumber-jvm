package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Argument;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.gherkin.StepType;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.Pickle.PickleStep;
import io.cucumber.messages.Messages.PickleStepArgument;
import io.cucumber.messages.Messages.PickleStepArgument.PickleDocString;
import io.cucumber.messages.Messages.PickleStepArgument.PickleTable;
import io.cucumber.plugin.event.Location;

final class GherkinMessagesStep implements Step {

    private final PickleStep pickleStep;
    private final Argument argument;
    private final String keyWord;
    private final StepType stepType;
    private final String previousGwtKeyWord;
    private final Messages.Location location;

    GherkinMessagesStep(
            PickleStep pickleStep,
            GherkinDialect dialect,
            String previousGwtKeyWord,
            Messages.Location location,
            String keyword
    ) {
        this.pickleStep = pickleStep;
        this.argument = extractArgument(pickleStep, location);
        this.keyWord = keyword;
        this.stepType = extractKeyWordType(keyWord, dialect);
        this.previousGwtKeyWord = previousGwtKeyWord;
        this.location = location;
    }

    private static Argument extractArgument(PickleStep pickleStep, Messages.Location location) {
        PickleStepArgument argument = pickleStep.getArgument();
        if (argument.hasDocString()) {
            PickleDocString docString = argument.getDocString();
            // TODO: Fix this work around
            return new GherkinMessagesDocStringArgument(docString, location.getLine() + 1);
        }
        if (argument.hasDataTable()) {
            PickleTable table = argument.getDataTable();
            return new GherkinMessagesDataTableArgument(table, location.getLine() + 1);
        }
        return null;
    }

    private static StepType extractKeyWordType(String keyWord, GherkinDialect dialect) {
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

    @Override
    public String getKeyword() {
        return keyWord;
    }

    @Override
    public int getLine() {
        return location.getLine();
    }

    @Override
    public Location getLocation() {
        return GherkinMessagesLocation.from(location);
    }

    @Override
    public StepType getType() {
        return stepType;
    }

    @Override
    public String getPreviousGivenWhenThenKeyword() {
        return previousGwtKeyWord;
    }

    @Override
    public String getId() {
        return pickleStep.getId();
    }

    @Override
    public Argument getArgument() {
        return argument;
    }

    @Override
    public String getText() {
        return pickleStep.getText();
    }

}
