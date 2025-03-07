package io.cucumber.core.gherkin.messages;

import io.cucumber.messages.types.RuleChild;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

final class GherkinMessagesRule implements Node.Rule {

    private final Node parent;
    private final io.cucumber.messages.types.Rule rule;
    private final List<Node> children;

    GherkinMessagesRule(Node parent, io.cucumber.messages.types.Rule rule) {
        this.parent = parent;
        this.rule = rule;
        this.children = rule.getChildren().stream()
                .map(RuleChild::getScenario)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(scenario -> {
                    if (!scenario.getExamples().isEmpty()) {
                        return new GherkinMessagesScenarioOutline(this, scenario);
                    } else {
                        return new GherkinMessagesScenario(this, scenario);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Node> getParent() {
        return Optional.of(parent);
    }

    @Override
    public Collection<Node> elements() {
        return children;
    }

    @Override
    public URI getUri() {
        return parent.getUri();
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
