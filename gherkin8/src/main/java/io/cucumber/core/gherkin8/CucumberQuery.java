package io.cucumber.core.gherkin8;

import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild.RuleChild;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;

import java.util.HashMap;
import java.util.Map;

public class CucumberQuery {
    private final Map<String, Step> gherkinStepById = new HashMap<>();
    private final Map<String, Scenario> gherkinScenarioById = new HashMap<>();

    public void update(GherkinDocument gherkinDocument) {
        for (FeatureChild featureChild : gherkinDocument.getFeature().getChildrenList()) {
            if (featureChild.hasBackground()) {
                this.updateBackground(
                    featureChild.getBackground(),
                    gherkinDocument.getUri()
                );
            }

            if (featureChild.hasScenario()) {
                this.updateScenario(
                    featureChild.getScenario(),
                    gherkinDocument.getUri()
                );
            }

            if (featureChild.hasRule()) {
                for (RuleChild ruleChild : featureChild.getRule().getChildrenList()) {
                    if (ruleChild.hasBackground()) {
                        this.updateBackground(
                            ruleChild.getBackground(),
                            gherkinDocument.getUri()
                        );
                    }

                    if (ruleChild.hasScenario()) {
                        this.updateScenario(
                            ruleChild.getScenario(),
                            gherkinDocument.getUri()
                        );
                    }
                }
            }
        }
    }

    private void updateScenario(Scenario scenario, String uri) {
        gherkinScenarioById.put(scenario.getId(), scenario);
        for (Step step : scenario.getStepsList()) {
            gherkinStepById.put(step.getId(), step);
        }
    }

    private void updateBackground(GherkinDocument.Feature.Background background, String uri) {
        for (Step step : background.getStepsList()) {
            gherkinStepById.put(step.getId(), step);
        }
    }

    public Step getGherkinStep(String id) {
        return gherkinStepById.get(id);
    }

    public Scenario getGherkinScenario(String id) {
        return gherkinScenarioById.get(id);
    }
}
