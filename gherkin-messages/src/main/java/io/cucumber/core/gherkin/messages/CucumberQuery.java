package io.cucumber.core.gherkin.messages;

import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.RuleChild;
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

    void update(GherkinDocument gherkinDocument) {
        for (FeatureChild featureChild : gherkinDocument.getFeature().getChildren()) {
            if (featureChild.getBackground() != null) {
                this.updateBackground(
                    featureChild.getBackground(),
                    gherkinDocument.getUri());
            }

            if (featureChild.getScenario() != null) {
                this.updateScenario(
                    featureChild.getScenario(),
                    gherkinDocument.getUri());
            }

            if (featureChild.getRule() != null) {
                for (RuleChild ruleChild : featureChild.getRule().getChildren()) {
                    if (ruleChild.getBackground() != null) {
                        this.updateBackground(
                            ruleChild.getBackground(),
                            gherkinDocument.getUri());
                    }

                    if (ruleChild.getScenario() != null) {
                        this.updateScenario(
                            ruleChild.getScenario(),
                            gherkinDocument.getUri());
                    }
                }
            }
        }
    }

    private void updateBackground(Background background, String uri) {
        updateStep(background.getSteps());
    }

    private void updateScenario(Scenario scenario, String uri) {
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
