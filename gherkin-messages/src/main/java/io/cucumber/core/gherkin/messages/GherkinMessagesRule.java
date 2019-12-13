package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.Rule;
import io.cucumber.core.gherkin.Node;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild.RuleChild;

import java.util.stream.Stream;

final class GherkinMessagesRule implements Rule {

    private final Messages.GherkinDocument.Feature.FeatureChild.Rule rule;

    GherkinMessagesRule(Messages.GherkinDocument.Feature.FeatureChild.Rule rule) {
        this.rule = rule;
    }

    @Override
    public Stream<Node> children() {
        return rule.getChildrenList().stream()
            .filter(RuleChild::hasScenario)
            .map(ruleChild -> {
                Messages.GherkinDocument.Feature.Scenario scenario = ruleChild.getScenario();
                if (scenario.getExamplesCount() > 0) {
                    return new GherkinMessagesScenarioOutline(scenario);
                } else {
                    return new GherkinMessagesScenario(scenario);
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
        return GherkinMessagesLocation.from(rule.getLocation());
    }
}
