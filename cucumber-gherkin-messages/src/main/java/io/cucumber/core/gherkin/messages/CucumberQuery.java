package io.cucumber.core.gherkin.messages;

import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Step;
import io.cucumber.messages.types.TableRow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

final class CucumberQuery {

    private final Map<String, Step> gherkinStepById = new HashMap<>();
    private final Map<String, Scenario> gherkinScenarioById = new HashMap<>();
    private final Map<String, Location> locationBySourceId = new HashMap<>();

    void update(Feature feature) {
        feature.getChildren().forEach(featureChild -> {
            featureChild.getBackground().ifPresent(this::updateBackground);
            featureChild.getScenario().ifPresent(this::updateScenario);
            featureChild.getRule().ifPresent(rule -> rule.getChildren().forEach(ruleChild -> {
                ruleChild.getBackground().ifPresent(this::updateBackground);
                ruleChild.getScenario().ifPresent(this::updateScenario);
            }));
        });
    }

    private void updateBackground(Background background) {
        updateStep(background.getSteps());
    }

    private void updateScenario(Scenario scenario) {
        gherkinScenarioById.put(requireNonNull(scenario.getId()), scenario);
        locationBySourceId.put(requireNonNull(scenario.getId()), scenario.getLocation());
        updateStep(scenario.getSteps());

        for (Examples examples : scenario.getExamples()) {
            for (TableRow tableRow : examples.getTableBody()) {
                this.locationBySourceId.put(requireNonNull(tableRow.getId()), tableRow.getLocation());
            }
        }
    }

    private void updateStep(List<Step> stepsList) {
        for (Step step : stepsList) {
            locationBySourceId.put(requireNonNull(step.getId()), step.getLocation());
            gherkinStepById.put(requireNonNull(step.getId()), step);
        }
    }

    Step getGherkinStep(String sourceId) {
        return requireNonNull(gherkinStepById.get(requireNonNull(sourceId)));
    }

    Scenario getGherkinScenario(String sourceId) {
        return requireNonNull(gherkinScenarioById.get(requireNonNull(sourceId)));
    }

    Location getLocation(String sourceId) {
        Location location = locationBySourceId.get(requireNonNull(sourceId));
        return requireNonNull(location);
    }

}
