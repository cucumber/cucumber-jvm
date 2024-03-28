package io.cucumber.core.plugin;

import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.TableRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultNamingStrategy implements NamingStrategy {
    private enum PrefixStrategy {LONG, SHORT}

    private enum ExampleStrategy {EXAMPLE_NUMBER, PICKLE_NAME}

    private enum FeatureNameStrategy {INCLUDE, EXCLUDE}

    private final PrefixStrategy prefixStrategy;
    private final ExampleStrategy exampleStrategy;
    private final FeatureNameStrategy featureNameStrategy;

    DefaultNamingStrategy(PrefixStrategy prefixStrategy, ExampleStrategy exampleStrategy, FeatureNameStrategy featureNameStrategy) {
        this.prefixStrategy = prefixStrategy;
        this.exampleStrategy = exampleStrategy;
        this.featureNameStrategy = featureNameStrategy;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String name(GherkinAstNodes nodes, Feature element) {
        return element.getName();
    }

    @Override
    public String name(GherkinAstNodes nodes, Rule element) {
        List<String> pieces = new ArrayList<>();
        if (prefixStrategy == PrefixStrategy.LONG) {
            featureName(nodes).ifPresent(pieces::add);
        }
        pieces.add(element.getName());
        return join(pieces);
    }

    @Override
    public String name(GherkinAstNodes nodes, Scenario element) {
        List<String> pieces = new ArrayList<>();
        if (prefixStrategy == PrefixStrategy.LONG) {
            featureName(nodes).ifPresent(pieces::add);
            nodes.rule().map(Rule::getName).ifPresent(pieces::add);
        }
        pieces.add(element.getName());
        return join(pieces);
    }

    @Override
    public String name(GherkinAstNodes nodes, Examples element) {
        List<String> pieces = new ArrayList<>();

        if (prefixStrategy == PrefixStrategy.LONG) {
            featureName(nodes).ifPresent(pieces::add);
            nodes.rule().map(Rule::getName).ifPresent(pieces::add);
            nodes.scenario().map(Scenario::getName).ifPresent(pieces::add);
        }
        pieces.add(element.getName());
        return join(pieces);
    }

    @Override
    public String name(GherkinAstNodes nodes, TableRow element) {
        List<String> pieces = new ArrayList<>();
        if (prefixStrategy == PrefixStrategy.LONG) {
            examplesName(nodes, pieces);
        }
        exampleNumber(nodes).ifPresent(pieces::add);
        return join(pieces);
    }

    public String name(GherkinAstNodes nodes, Pickle element) {
        List<String> pieces = new ArrayList<>();
        if (prefixStrategy == PrefixStrategy.LONG) {
            examplesName(nodes, pieces);
        }
        if (exampleStrategy == ExampleStrategy.EXAMPLE_NUMBER) {
            exampleNumber(nodes).ifPresent(pieces::add);
        } else {
            pieces.add(element.getName());
        }
        return join(pieces);
    }

    private void examplesName(GherkinAstNodes nodes, List<String> pieces) {
        featureName(nodes).ifPresent(pieces::add);
        nodes.rule().map(Rule::getName).ifPresent(pieces::add);
        nodes.scenario().map(Scenario::getName).ifPresent(pieces::add);
        nodes.examples().map(Examples::getName).ifPresent(pieces::add);
    }

    private Optional<String> featureName(GherkinAstNodes nodes) {
        if (featureNameStrategy == FeatureNameStrategy.INCLUDE) {
            nodes.feature().map(Feature::getName);
        }
        return Optional.empty();
    }

    private static Optional<String> exampleNumber(GherkinAstNodes nodes) {
        String examplesPrefix = nodes.examplesIndex()
                .map(examplesIndex -> examplesIndex + 1)
                .map(examplesIndex -> examplesIndex + ".")
                .orElse("");
        return nodes.exampleIndex()
                .map(exampleIndex -> exampleIndex + 1)
                .map(exampleSuffix -> "Example #" + examplesPrefix + exampleSuffix);
    }

    private static String join(List<String> pieces) {
        return pieces.stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" - "));
    }

    public static class Builder {

        private DefaultNamingStrategy.PrefixStrategy prefixStrategy = PrefixStrategy.LONG;
        private DefaultNamingStrategy.ExampleStrategy exampleStrategy = ExampleStrategy.EXAMPLE_NUMBER;
        private DefaultNamingStrategy.FeatureNameStrategy featureNameStrategy = FeatureNameStrategy.EXCLUDE;

        Builder() {

        }

        public Builder prefixStrategy(PrefixStrategy prefixStrategy) {
            this.prefixStrategy = prefixStrategy;
            return this;
        }

        public Builder exampleStrategy(ExampleStrategy exampleStrategy) {
            this.exampleStrategy = exampleStrategy;
            return this;
        }

        public Builder featureNameStrategy(FeatureNameStrategy featureNameStrategy) {
            this.featureNameStrategy = featureNameStrategy;
            return this;
        }

        public DefaultNamingStrategy build() {
            return new DefaultNamingStrategy(prefixStrategy, exampleStrategy, featureNameStrategy);
        }
    }
}
