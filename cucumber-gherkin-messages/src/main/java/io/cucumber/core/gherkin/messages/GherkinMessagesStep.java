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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

final class GherkinMessagesStep implements Step {

    private final PickleStep pickleStep;
    private final List<Argument> arguments;
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
        this.arguments = extractArgument(pickleStep, location);
        this.keyword = keyword;
        this.stepType = extractKeyWordType(this.keyword, dialect);
        this.previousGwtKeyword = previousGwtKeyword;
        this.location = location;
    }

    private static List<Argument> extractArgument(PickleStep pickleStep, Location location) {
        return Stream.of(pickleStep.getArgument())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(argument -> {
                    var dt = argument.getDataTable();
                    var ds = argument.getDocString();
                    var arguments = new ArrayList<Argument>(2);

                    // Workaround, we don't know the location of the table.
                    dt.map(pickleTable -> new GherkinMessagesDataTableArgument(pickleTable, location.getLine() + 1))
                            .ifPresent(arguments::add);
                    ds.map(pickleDocString -> new GherkinMessagesDocStringArgument(pickleDocString,
                        location.getLine() + 1))
                            .ifPresent(arguments::add);

                    var dataTableIndex = dt.flatMap(PickleTable::getArgumentIndex).orElse(Integer.MAX_VALUE);
                    var docStringIndex = ds.flatMap(PickleDocString::getArgumentIndex).orElse(Integer.MAX_VALUE);

                    if (docStringIndex < dataTableIndex) {
                        Collections.reverse(arguments);
                    }
                    return arguments.stream();
                })
                .toList();
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
        return arguments.isEmpty() ? null : arguments.get(0);
    }

    @Override
    public List<Argument> getArguments() {
        return arguments;
    }

    @Override
    public String getText() {
        return pickleStep.getText();
    }

}
