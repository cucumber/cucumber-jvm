package io.cucumber.core.gherkin.messages;

import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

final class GherkinMessagesRule implements Node.Rule {

    private final io.cucumber.messages.types.Rule rule;
    private final List<Node> children;

    GherkinMessagesRule(io.cucumber.messages.types.Rule rule) {
        this.rule = rule;
        this.children = rule.getChildren().stream()
                .filter(ruleChild -> ruleChild.getScenario() != null)
                .map(ruleChild -> {
                    io.cucumber.messages.types.Scenario scenario = ruleChild.getScenario();
                    if (!scenario.getExamples().isEmpty()) {
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
