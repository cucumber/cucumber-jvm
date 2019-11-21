package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.CucumberPickle;
import io.cucumber.core.gherkin.CucumberStep;
import io.cucumber.core.gherkin.StepType;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;
import io.cucumber.messages.Messages.Pickle.PickleStep;
import io.cucumber.messages.Messages.Pickle.PickleTag;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wraps {@link Messages.Pickle} to avoid exposing the gherkin library to all of
 * Cucumber.
 */
public final class Gherkin8CucumberPickle implements CucumberPickle {

    private final Messages.Pickle pickle;
    private final List<CucumberStep> steps;
    private final URI uri;
    private final CucumberQuery cucumberQuery;

    Gherkin8CucumberPickle(Messages.Pickle pickle, URI uri, GherkinDialect dialect, CucumberQuery cucumberQuery) {
        this.pickle = pickle;
        this.uri = uri;
        this.cucumberQuery = cucumberQuery;
        this.steps = createCucumberSteps(pickle, dialect, cucumberQuery);
    }

    private static List<CucumberStep> createCucumberSteps(Messages.Pickle pickle, GherkinDialect dialect, CucumberQuery cucumberQuery) {
        List<CucumberStep> list = new ArrayList<>();
        String previousGivenWhenThen = dialect.getGivenKeywords()
            .stream()
            .filter(s -> !StepType.isAstrix(s))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No Given keyword for dialect: " + dialect.getName()));

        for (PickleStep pickleStep : pickle.getStepsList()) {
            String gherkinStepId = pickleStep.getSourceIds(0);
            Step gherkinStep = cucumberQuery.getGherkinStep(gherkinStepId);
            int stepLine = gherkinStep.getLocation().getLine();
            String keyword = gherkinStep.getKeyword();

            CucumberStep cucumberStep = new Gherkin8CucumberStep(pickleStep, dialect, previousGivenWhenThen, stepLine, keyword);
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
        return cucumberQuery.getGherkinScenario(pickle.getSourceIds(0)).getLocation().getLine();
    }

    /**
     * Returns the line in feature file of the Scenario this pickle was created
     * from. If this pickle was created from a Scenario Outline this line is the
     *
     * @return line in the feature file
     */
    @Override
    public int getScenarioLine() {
        throw new UnsupportedOperationException("Not supported");
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

    @Override
    public String getId() {
        return pickle.getId();
    }


}
