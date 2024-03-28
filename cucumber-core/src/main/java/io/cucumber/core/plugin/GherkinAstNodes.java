package io.cucumber.core.plugin;

import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.TableRow;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

class GherkinAstNodes {
    private final GherkinDocument document;
    private final Feature feature;
    private final Rule rule;
    private final Scenario scenario;
    private final Examples examples;
    private final TableRow example;
    private final Integer examplesIndex;
    private final Integer exampleIndex;

    GherkinAstNodes(GherkinDocument document, Feature feature, Rule rule, Scenario scenario) {
        this(document, feature, rule, scenario, null, null, null, null);
    }

    GherkinAstNodes(GherkinDocument document, Feature feature, Rule rule, Scenario scenario, Integer examplesIndex, Examples examples, Integer exampleIndex, TableRow example) {
        this.document = requireNonNull(document);
        this.feature = feature;
        this.rule = rule;
        this.scenario = scenario;
        this.examplesIndex = examplesIndex;
        this.examples = examples;
        this.exampleIndex = exampleIndex;
        this.example = example;
    }

    GherkinDocument document() {
        return document;
    }

    Optional<Feature> feature() {
        return Optional.ofNullable(feature);
    }

    Optional<Rule> rule() {
        return Optional.ofNullable(rule);
    }

    Optional<Scenario> scenario() {
        return Optional.ofNullable(scenario);
    }

    Optional<Examples> examples() {
        return Optional.ofNullable(examples);
    }

    Optional<TableRow> example() {
        return Optional.ofNullable(example);
    }

    Optional<Integer> examplesIndex() {
        return Optional.ofNullable(examplesIndex);
    }

    Optional<Integer> exampleIndex() {
        return Optional.ofNullable(exampleIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GherkinAstNodes that = (GherkinAstNodes) o;
        return document.equals(that.document) && feature.equals(that.feature) && Objects.equals(rule, that.rule) && scenario.equals(that.scenario) && Objects.equals(examples, that.examples) && Objects.equals(example, that.example) && Objects.equals(examplesIndex, that.examplesIndex) && Objects.equals(exampleIndex, that.exampleIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(document, feature, rule, scenario, examples, example, examplesIndex, exampleIndex);
    }
}
