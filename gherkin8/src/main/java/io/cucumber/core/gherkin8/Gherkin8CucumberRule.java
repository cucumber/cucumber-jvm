package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.CucumberLocation;
import io.cucumber.core.gherkin.CucumberRule;
import io.cucumber.core.gherkin.Node;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild.Rule;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild.RuleChild;

import java.util.stream.Stream;

final class Gherkin8CucumberRule implements CucumberRule {

    private final Rule rule;

    Gherkin8CucumberRule(Rule rule) {
        this.rule = rule;
    }

    @Override
    public Stream<Node> children() {
        return rule.getChildrenList().stream()
            .filter(RuleChild::hasScenario)
            .map(ruleChild -> {
                Messages.GherkinDocument.Feature.Scenario scenario = ruleChild.getScenario();
                if (scenario.getExamplesCount() > 0) {
                    return new Gherkin8CucumberScenarioOutline(scenario);
                } else {
                    return new Gherkin8CucumberScenario(scenario);
                }
            });
    }

    @Override
    public String getKeyWord() {
        return rule.getKeyword();
    }

    @Override
    public String getName() {
        return rule.getName();
    }

    @Override
    public CucumberLocation getLocation() {
        return Gherkin8CucumberLocation.from(rule.getLocation());
    }
}
