package io.cucumber.core.gherkin.messages;

import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.TableRow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

final class CucumberQuery {

    private final Map<String, Rule> ruleByScenarioId = new HashMap<>();
    private final Map<String, Examples> examplesByExampleId = new HashMap<>();
    private final Map<String, Feature> featureByScenarioId = new HashMap<>();
    private final Map<String, Step> gherkinStepById = new HashMap<>();
    private final Map<String, Scenario> gherkinScenarioById = new HashMap<>();
    private final Map<String, Location> locationBySourceId = new HashMap<>();

    void update(Feature feature) {
        feature.getChildren().forEach(featureChild -> {
            featureChild.getBackground().ifPresent(this::updateBackground);
            featureChild.getScenario().ifPresent(scenario -> updateScenario(feature, null, scenario));
            featureChild.getRule().ifPresent(rule -> {
                rule.getChildren().forEach(ruleChild -> {
                    ruleChild.getBackground().ifPresent(this::updateBackground);
                    ruleChild.getScenario().ifPresent(scenario -> updateScenario(feature, rule, scenario));
                });
            });
        });
    }

    private void updateBackground(Background background) {
        updateStep(background.getSteps());
    }

    private void updateScenario(Feature feature, Rule rule, Scenario scenario) {
        gherkinScenarioById.put(requireNonNull(scenario.getId()), scenario);
        locationBySourceId.put(requireNonNull(scenario.getId()), scenario.getLocation());
        updateStep(scenario.getSteps());

        for (Examples examples : scenario.getExamples()) {
            for (TableRow tableRow : examples.getTableBody()) {
                this.examplesByExampleId.put(tableRow.getId(), examples);
                this.locationBySourceId.put(tableRow.getId(), tableRow.getLocation());
            }
        }

        if (rule != null) {
            ruleByScenarioId.put(scenario.getId(), rule);
        }

        featureByScenarioId.put(scenario.getId(), feature);
    }

    private void updateStep(List<Step> stepsList) {
        for (Step step : stepsList) {
            locationBySourceId.put(requireNonNull(step.getId()), step.getLocation());
            gherkinStepById.put(requireNonNull(step.getId()), step);
        }
    }

    Step getStepBy(PickleStep pickleStep) {
        requireNonNull(pickleStep);
        String gherkinStepId = pickleStep.getAstNodeIds().get(0);
        return requireNonNull(gherkinStepById.get(gherkinStepId));
    }

    Scenario getScenarioBy(Pickle pickle) {
        requireNonNull(pickle);
        return requireNonNull(gherkinScenarioById.get(pickle.getAstNodeIds().get(0)));
    }

    Optional<Rule> findRuleBy(Pickle pickle) {
        requireNonNull(pickle);
        Scenario scenario = getScenarioBy(pickle);
        return Optional.ofNullable(ruleByScenarioId.get(scenario.getId()));
    }

    Location getLocationBy(Pickle pickle) {
        requireNonNull(pickle);
        List<String> sourceIds = pickle.getAstNodeIds();
        String sourceId = sourceIds.get(sourceIds.size() - 1);
        Location location = locationBySourceId.get(sourceId);
        return requireNonNull(location);
    }

    Optional<Feature> findFeatureBy(Pickle pickle) {
        requireNonNull(pickle);
        Scenario scenario = getScenarioBy(pickle);
        return Optional.ofNullable(featureByScenarioId.get(scenario.getId()));
    }

    Optional<Examples> findExamplesBy(Pickle pickle) {
        requireNonNull(pickle);
        List<String> sourceIds = pickle.getAstNodeIds();
        String sourceId = sourceIds.get(sourceIds.size() - 1);
        return Optional.ofNullable(examplesByExampleId.get(sourceId));
    }
}
