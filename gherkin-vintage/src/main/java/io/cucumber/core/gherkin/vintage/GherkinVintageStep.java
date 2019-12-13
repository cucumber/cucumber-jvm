package io.cucumber.core.gherkin.vintage;

import gherkin.GherkinDialect;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import io.cucumber.core.gherkin.Argument;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.gherkin.StepType;

import java.util.stream.Collectors;

final class GherkinVintageStep implements Step {

    private final PickleStep step;
    private final Argument argument;
    private final String keyWord;
    private final StepType stepType;
    private final String previousGwtKeyWord;
    private final String uri;

    GherkinVintageStep(PickleStep step, GherkinDocument document, GherkinDialect dialect, String previousGwtKeyWord, String uri) {
        this.step = step;
        this.argument = extractArgument(step);
        this.keyWord = extractKeyWord(document);
        this.stepType = extractKeyWordType(keyWord, dialect);
        this.previousGwtKeyWord = previousGwtKeyWord;
        this.uri = uri;
    }

    private String extractKeyWord(GherkinDocument document) {
        return document.getFeature().getChildren().stream()
            .flatMap(scenarioDefinition -> scenarioDefinition.getSteps().stream())
            .filter(step -> step.getLocation().getLine() == getLine())
            .findFirst()
            .map(gherkin.ast.Step::getKeyword)
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
        if (pickleStep.getArgument().isEmpty()) {
            return null;
        }
        gherkin.pickles.Argument argument = pickleStep.getArgument().get(0);
        if (argument instanceof PickleString) {
            PickleString docString = (PickleString) argument;
            return new GherkinVintageDocStringArgument(docString);
        }
        if (argument instanceof PickleTable) {
            PickleTable table = (PickleTable) argument;
            return new GherkinVintageDataTableArgument(table);
        }
        return null;
    }

    @Override
    public int getLine() {
        int last = step.getLocations().size() - 1;
        return step.getLocations().get(last).getLine();
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
        return step.getText();
    }

    @Override
    public String getId() {
        String lineNumbers = this.step.getLocations().stream()
            .map(s -> String.valueOf(s.getLine()))
            .collect(Collectors.joining(":"));
        return uri + ":" + lineNumbers;
    }
}
