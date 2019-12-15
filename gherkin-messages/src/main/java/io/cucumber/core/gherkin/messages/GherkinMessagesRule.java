package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.Node;
import io.cucumber.core.gherkin.Rule;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild.RuleChild;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

final class GherkinMessagesRule implements Rule {

    private final Messages.GherkinDocument.Feature.FeatureChild.Rule rule;
    private final List<Node> children;

    GherkinMessagesRule(Messages.GherkinDocument.Feature.FeatureChild.Rule rule) {
        this.rule = rule;
        this.children = rule.getChildrenList().stream()
            .filter(RuleChild::hasScenario)
            .map(ruleChild -> {
                Messages.GherkinDocument.Feature.Scenario scenario = ruleChild.getScenario();
                if (scenario.getExamplesCount() > 0) {
                    return new GherkinMessagesScenarioOutline(scenario);
                } else {
                    return new GherkinMessagesScenario(scenario);
                }
            })
            .collect(Collectors.toList());
    }

    @Override
    public Collection<Node> children() {
        return children;
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
