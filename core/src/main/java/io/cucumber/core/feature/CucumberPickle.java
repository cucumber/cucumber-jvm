package io.cucumber.core.feature;

import gherkin.GherkinDialect;
import gherkin.ast.GherkinDocument;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wraps {@link PickleEvent} to avoid exposing the gherkin library to all of
 * Cucumber.
 */
public final class CucumberPickle {

    private final PickleEvent pickleEvent;
    private final List<CucumberStep> steps;

    CucumberPickle(PickleEvent pickleEvent, GherkinDocument gherkinDocument, GherkinDialect dialect) {
        this.pickleEvent = pickleEvent;
        this.steps = createCucumberSteps(pickleEvent, gherkinDocument, dialect);
    }

    private static List<CucumberStep> createCucumberSteps(PickleEvent pickleEvent, GherkinDocument gherkinDocument, GherkinDialect dialect) {
        List<CucumberStep> list = new ArrayList<>();
        String previousGivenWhenThen = dialect.getGivenKeywords()
            .stream()
            .filter(s -> !StepType.isAstrix(s))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No Given keyword for dialect: " + dialect.getName()));

        for (PickleStep pickleStep : pickleEvent.pickle.getSteps()) {
            CucumberStep cucumberStep = new CucumberStep(pickleStep, gherkinDocument, dialect, previousGivenWhenThen);
            if (cucumberStep.getStepType().isGivenWhenThen()) {
                previousGivenWhenThen = cucumberStep.getKeyWord();
            }
            list.add(cucumberStep);
        }
        return list;
    }

    public String getLanguage() {
        return pickleEvent.pickle.getLanguage();
    }

    public String getName() {
        return pickleEvent.pickle.getName();
    }

    /**
     * Returns the line in feature file of the Scenario this pickle was created
     * from. If this pickle was created from a Scenario Outline this line is the
     * line in the Example section used to fill in the place holders.
     *
     * @return line in the feature file
     */
    public int getLine() {
        return pickleEvent.pickle.getLocations().get(0).getLine();
    }

    /**
     * Returns the line in feature file of the Scenario this pickle was created
     * from. If this pickle was created from a Scenario Outline this line is the
     *
     * @return line in the feature file
     */
    public int getScenarioLine() {
        List<PickleLocation> stepLocations = pickleEvent.pickle.getLocations();
        return stepLocations.get(stepLocations.size() - 1).getLine();
    }

    public List<CucumberStep> getSteps() {
        return steps;
    }

    public List<String> getTags() {
        return pickleEvent.pickle.getTags().stream().map(PickleTag::getName).collect(Collectors.toList());
    }

    public String getUri() {
        return pickleEvent.uri;
    }


}
