package io.cucumber.core.gherkin5;

import gherkin.GherkinDialect;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import io.cucumber.core.gherkin.CucumberLocation;
import io.cucumber.core.gherkin.CucumberPickle;
import io.cucumber.core.gherkin.CucumberStep;
import io.cucumber.core.gherkin.StepType;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.cucumber.core.gherkin5.Gherkin5CucumberLocation.from;
import static java.util.stream.Collectors.toList;

/**
 * Wraps {@link Pickle} to avoid exposing the gherkin library to all of
 * Cucumber.
 */
final class Gherkin5CucumberPickle implements CucumberPickle {

    private final Pickle pickle;
    private final List<CucumberStep> steps;
    private final URI uri;

    Gherkin5CucumberPickle(Pickle pickle, URI uri, GherkinDocument document, GherkinDialect dialect) {
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
            CucumberStep cucumberStep = new Gherkin5CucumberStep(step, document, dialect, previousGivenWhenThen);
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


    @Override
    public CucumberLocation getLocation() {
        return from(pickle.getLocations().get(0));
    }

    @Override
    public CucumberLocation getScenarioLocation() {
        int last = pickle.getLocations().size() - 1;
        return from(pickle.getLocations().get(last));
    }

    @Override
    public List<CucumberStep> getSteps() {
        return steps;
    }

    @Override
    public List<String> getTags() {
        return pickle.getTags().stream()
            .map(PickleTag::getName)
            .collect(toList());
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public String getId() {
        return pickle.getName() + ":" + pickle.getLocations()
            .stream()
            .map(l -> String.valueOf(l.getLine()))
            .collect(Collectors.joining(":"));
    }


}
