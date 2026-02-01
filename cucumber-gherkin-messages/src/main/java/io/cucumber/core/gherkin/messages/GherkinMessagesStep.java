package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Argument;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.gherkin.StepType;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.messages.types.PickleDocString;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.PickleTable;
import io.cucumber.plugin.event.Location;
import org.jspecify.annotations.Nullable;

final class GherkinMessagesStep implements Step {

    private final PickleStep pickleStep;
    private final @Nullable Argument argument;
    private final String keyword;
    private final StepType stepType;
    private final String previousGwtKeyword;
    private final Location location;

    GherkinMessagesStep(
            PickleStep pickleStep,
            GherkinDialect dialect,
            String previousGwtKeyword,
            Location location,
            String keyword
    ) {
        this.pickleStep = pickleStep;
        this.argument = extractArgument(pickleStep, location);
        this.keyword = keyword;
        this.stepType = extractKeyWordType(this.keyword, dialect);
        this.previousGwtKeyword = previousGwtKeyword;
        this.location = location;
    }

    private static @Nullable Argument extractArgument(PickleStep pickleStep, Location location) {
        return pickleStep.getArgument()
                .map(argument -> {
                    if (argument.getDocString().isPresent()) {
                        PickleDocString docString = argument.getDocString().get();
                        // TODO: Fix this work around
                        return new GherkinMessagesDocStringArgument(docString, location.getLine() + 1);
                    }
                    if (argument.getDataTable().isPresent()) {
                        PickleTable table = argument.getDataTable().get();
                        return new GherkinMessagesDataTableArgument(table, location.getLine() + 1);
                    }
                    return null;
                }).orElse(null);
    }

    private static StepType extractKeyWordType(String keyword, GherkinDialect dialect) {
        if (StepType.isAstrix(keyword)) {
            return StepType.OTHER;
        }
        if (dialect.getGivenKeywords().contains(keyword)) {
            return StepType.GIVEN;
        }
        if (dialect.getWhenKeywords().contains(keyword)) {
            return StepType.WHEN;
        }
        if (dialect.getThenKeywords().contains(keyword)) {
            return StepType.THEN;
        }
        if (dialect.getAndKeywords().contains(keyword)) {
            return StepType.AND;
        }
        if (dialect.getButKeywords().contains(keyword)) {
            return StepType.BUT;
        }
        throw new IllegalStateException("Keyword " + keyword + " was neither given, when, then, and, but nor *");
    }

    @Override
    public String getKeyword() {
        return keyword;
    }

    @Override
    public int getLine() {
        return location.getLine();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public StepType getType() {
        return stepType;
    }

    @Override
    public String getPreviousGivenWhenThenKeyword() {
        return previousGwtKeyword;
    }

    @Override
    public String getId() {
        return pickleStep.getId();
    }

    @Override
    public @Nullable Argument getArgument() {
        return argument;
    }

    @Override
    public String getText() {
        return pickleStep.getText();
    }

}
