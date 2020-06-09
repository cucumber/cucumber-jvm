package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.gherkin.StepType;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;
import io.cucumber.messages.Messages.Pickle.PickleStep;
import io.cucumber.messages.Messages.Pickle.PickleTag;
import io.cucumber.plugin.event.Location;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wraps {@link Messages.Pickle} to avoid exposing the gherkin library to all of
 * Cucumber.
 */
final class GherkinMessagesPickle implements Pickle {

    private final Messages.Pickle pickle;
    private final List<Step> steps;
    private final URI uri;
    private final CucumberQuery cucumberQuery;

    GherkinMessagesPickle(Messages.Pickle pickle, URI uri, GherkinDialect dialect, CucumberQuery cucumberQuery) {
        this.pickle = pickle;
        this.uri = uri;
        this.cucumberQuery = cucumberQuery;
        this.steps = createCucumberSteps(pickle, dialect, this.cucumberQuery);
    }

    private static List<Step> createCucumberSteps(
            Messages.Pickle pickle,
            GherkinDialect dialect,
            CucumberQuery cucumberQuery
    ) {
        List<Step> list = new ArrayList<>();
        String previousGivenWhenThen = dialect.getGivenKeywords()
                .stream()
                .filter(s -> !StepType.isAstrix(s))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Given keyword for dialect: " + dialect.getName()));

        for (PickleStep pickleStep : pickle.getStepsList()) {
            String gherkinStepId = pickleStep.getAstNodeIds(0);
            Messages.GherkinDocument.Feature.Step gherkinStep = cucumberQuery.getGherkinStep(gherkinStepId);
            Messages.Location location = gherkinStep.getLocation();
            String keyword = gherkinStep.getKeyword();

            Step step = new GherkinMessagesStep(pickleStep, dialect, previousGivenWhenThen, location, keyword);
            if (step.getType().isGivenWhenThen()) {
                previousGivenWhenThen = step.getKeyword();
            }
            list.add(step);
        }
        return list;
    }

    @Override
    public String getKeyword() {
        return cucumberQuery.getGherkinScenario(pickle.getAstNodeIds(0)).getKeyword();
    }

    @Override
    public String getLanguage() {
        return pickle.getLanguage();
    }

    @Override
    public String getName() {
        return pickle.getName();
    }

    @Override
    public Location getLocation() {
        List<String> sourceIds = pickle.getAstNodeIdsList();
        String sourceId = sourceIds.get(sourceIds.size() - 1);
        Messages.Location location = cucumberQuery.getLocation(sourceId);
        return GherkinMessagesLocation.from(location);
    }

    @Override
    public Location getScenarioLocation() {
        String sourceId = pickle.getAstNodeIds(0);
        Scenario scenario = cucumberQuery.getGherkinScenario(sourceId);
        Messages.Location location = scenario.getLocation();
        return GherkinMessagesLocation.from(location);
    }

    @Override
    public List<Step> getSteps() {
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
