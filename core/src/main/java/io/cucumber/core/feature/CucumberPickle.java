package io.cucumber.core.feature;

import gherkin.GherkinDialect;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wraps {@link Pickle} to avoid exposing the gherkin library to all of
 * Cucumber.
 */
public final class CucumberPickle {

    private final Pickle pickle;
    private final List<CucumberStep> steps;
    private final URI uri;

    CucumberPickle(Pickle pickle, URI uri, GherkinDocument document, GherkinDialect dialect) {
        this.pickle = pickle;
        this.uri = uri;
        this.steps = createCucumberSteps(pickle, document, dialect);
    }

    private static List<CucumberStep> createCucumberSteps(Pickle pickle, GherkinDocument document, GherkinDialect dialect) {
        List<CucumberStep> list = new ArrayList<>();
        String previousGivenWhenThen = dialect.getGivenKeywords()
            .stream()
            .filter(s -> !StepType.isAstrix(s))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No Given keyword for dialect: " + dialect.getName()));

        for (PickleStep step : pickle.getSteps()) {
            CucumberStep cucumberStep = new CucumberStep(step, document, dialect, previousGivenWhenThen);
            if (cucumberStep.getStepType().isGivenWhenThen()) {
                previousGivenWhenThen = cucumberStep.getKeyWord();
            }
            list.add(cucumberStep);
        }
        return list;
    }

    public String getLanguage() {
        return pickle.getLanguage();
    }

    public String getName() {
        return pickle.getName();
    }

    /**
     * Returns the line in feature file of the Scenario this pickle was created
     * from. If this pickle was created from a Scenario Outline this line is the
     * line in the Example section used to fill in the place holders.
     *
     * @return line in the feature file
     */
    public int getLine() {
        return pickle.getLocations().get(0).getLine();
    }

    /**
     * Returns the line in feature file of the Scenario this pickle was created
     * from. If this pickle was created from a Scenario Outline this line is the
     *
     * @return line in the feature file
     */
    public int getScenarioLine() {
        List<PickleLocation> stepLocations = pickle.getLocations();
        return stepLocations.get(stepLocations.size() - 1).getLine();
    }

    public List<CucumberStep> getSteps() {
        return steps;
    }

    public List<String> getTags() {
        return pickle.getTags().stream().map(PickleTag::getName).collect(Collectors.toList());
    }

    public URI getUri() {
        return uri;
    }


}
