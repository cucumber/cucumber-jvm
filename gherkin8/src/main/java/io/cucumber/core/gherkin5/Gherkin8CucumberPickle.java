package io.cucumber.core.gherkin5;

import io.cucumber.core.gherkin.CucumberPickle;
import io.cucumber.core.gherkin.CucumberStep;
import io.cucumber.core.gherkin.StepType;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.Location;
import io.cucumber.messages.Messages.Pickle.PickleTag;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wraps {@link Pickle} to avoid exposing the gherkin library to all of
 * Cucumber.
 */
public final class Gherkin8CucumberPickle implements CucumberPickle {

    private final Messages.Pickle pickle;
    private final List<CucumberStep> steps;
    private final URI uri;

    Gherkin8CucumberPickle(Messages.Pickle pickle, URI uri, Messages.GherkinDocument document, GherkinDialect dialect) {
        this.pickle = pickle;
        this.uri = uri;
        this.steps = createCucumberSteps(pickle, document, dialect);
    }

    private static List<CucumberStep> createCucumberSteps(Messages.Pickle pickle, Messages.GherkinDocument document, GherkinDialect dialect) {
        List<CucumberStep> list = new ArrayList<>();
        String previousGivenWhenThen = dialect.getGivenKeywords()
            .stream()
            .filter(s -> !StepType.isAstrix(s))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No Given keyword for dialect: " + dialect.getName()));

        for (Messages.Pickle.PickleStep step : pickle.getStepsList()) {
            CucumberStep cucumberStep = new Gherkin8CucumberStep(step, document, dialect, previousGivenWhenThen);
            if (cucumberStep.getStepType().isGivenWhenThen()) {
                previousGivenWhenThen = cucumberStep.getKeyWord();
            }
            list.add(cucumberStep);
        }
        return list;
    }

    @Override
    public String getLanguage() {
        return pickle.getLanguage();
    }

    @Override
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
    @Override
    public int getLine() {
        return pickle.getLocationsList().get(0).getLine();
    }

    /**
     * Returns the line in feature file of the Scenario this pickle was created
     * from. If this pickle was created from a Scenario Outline this line is the
     *
     * @return line in the feature file
     */
    @Override
    public int getScenarioLine() {
        List<Location> stepLocations = pickle.getLocationsList();
        return stepLocations.get(stepLocations.size() - 1).getLine();
    }

    @Override
    public List<CucumberStep> getSteps() {
        return steps;
    }

    @Override
    public List<String> getTags() {
        return pickle.getTagsList().stream().map(PickleTag::getName).collect(Collectors.toList());
    }

    @Override
    public URI getUri() {
        return uri;
    }


}
