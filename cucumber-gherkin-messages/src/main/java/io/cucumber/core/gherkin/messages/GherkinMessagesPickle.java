package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.gherkin.StepType;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.PickleTag;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Scenario;
import io.cucumber.plugin.event.Location;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Wraps {@link Pickle} to avoid exposing the gherkin library to all of
 * Cucumber.
 */
final class GherkinMessagesPickle implements Pickle {

    private final io.cucumber.messages.types.Pickle pickle;
    private final List<Step> steps;
    private final URI uri;
    private final CucumberQuery cucumberQuery;

    GherkinMessagesPickle(
            io.cucumber.messages.types.Pickle pickle, URI uri, GherkinDialect dialect, CucumberQuery cucumberQuery
    ) {
        this.pickle = pickle;
        this.uri = uri;
        this.cucumberQuery = cucumberQuery;
        this.steps = createCucumberSteps(pickle, dialect, this.cucumberQuery);
    }

    private static List<Step> createCucumberSteps(
            io.cucumber.messages.types.Pickle pickle,
            GherkinDialect dialect,
            CucumberQuery cucumberQuery
    ) {
        List<Step> list = new ArrayList<>();
        String previousGivenWhenThen = dialect.getGivenKeywords()
                .stream()
                .filter(s -> !StepType.isAstrix(s))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Given keyword for dialect: " + dialect.getName()));

        for (io.cucumber.messages.types.PickleStep pickleStep : pickle.getSteps()) {
            io.cucumber.messages.types.Step gherkinStep = cucumberQuery.getStepBy(pickleStep);
            Location location = GherkinMessagesLocation.from(gherkinStep.getLocation());
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
        return cucumberQuery.getScenarioBy(pickle).getKeyword();
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
        return GherkinMessagesLocation.from(cucumberQuery.getLocationBy(pickle));
    }

    @Override
    public Location getScenarioLocation() {
        Scenario scenario = cucumberQuery.getScenarioBy(pickle);
        return GherkinMessagesLocation.from(scenario.getLocation());
    }

    @Override
    public Optional<Location> getRuleLocation() {
        return cucumberQuery.findRuleBy(pickle)
                .map(Rule::getLocation)
                .map(GherkinMessagesLocation::from);
    }

    @Override
    public Optional<Location> getFeatureLocation() {
        return cucumberQuery.findFeatureBy(pickle)
                .map(Feature::getLocation)
                .map(GherkinMessagesLocation::from);
    }

    @Override
    public Optional<Location> getExamplesLocation() {
        return cucumberQuery.findExamplesBy(pickle)
                .map(Examples::getLocation)
                .map(GherkinMessagesLocation::from);
    }

    @Override
    public List<Step> getSteps() {
        return steps;
    }

    @Override
    public List<String> getTags() {
        return pickle.getTags().stream().map(PickleTag::getName).collect(Collectors.toList());
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
