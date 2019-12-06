package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.Rule;
import io.cucumber.core.gherkin.Node;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild.RuleChild;

import java.util.stream.Stream;

final class Gherkin8Rule implements Rule {

    private final Messages.GherkinDocument.Feature.FeatureChild.Rule rule;

    Gherkin8Rule(Messages.GherkinDocument.Feature.FeatureChild.Rule rule) {
        this.rule = rule;
    }

    @Override
    public Stream<Node> children() {
        return rule.getChildrenList().stream()
            .filter(RuleChild::hasScenario)
            .map(ruleChild -> {
                Messages.GherkinDocument.Feature.Scenario scenario = ruleChild.getScenario();
                if (scenario.getExamplesCount() > 0) {
                    return new Gherkin8ScenarioOutline(scenario);
                } else {
                    return new Gherkin8Scenario(scenario);
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
    public Location getLocation() {
        return Gherkin8Location.from(rule.getLocation());
    }
}
