package io.cucumber.core.gherkin.messages;

import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild.RuleChild;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

final class GherkinMessagesRule implements Node.Rule {

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
    public Collection<Node> elements() {
        return children;
    }

    @Override
    public Location getLocation() {
        return GherkinMessagesLocation.from(rule.getLocation());
    }

    @Override
    public Optional<String> getKeyword() {
        return Optional.of(rule.getKeyword());
    }

    @Override
    public Optional<String> getName() {
        String name = rule.getName();
        return name.isEmpty() ? Optional.empty() : Optional.of(name);
    }

}
