package io.cucumber.core.plugin;

import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Scenario;
import io.cucumber.query.NamingStrategy;

import static io.cucumber.core.plugin.TestSourcesModel.convertToId;

class IdNamingVisitor implements NamingStrategy.NamingVisitor {
    @Override
    public String accept(Feature feature) {
        return convertToId(feature.getName());
    }

    @Override
    public String accept(Rule rule) {
        return convertToId(rule.getName());
    }

    @Override
    public String accept(Scenario scenario) {
        return convertToId(scenario.getName());
    }

    @Override
    public String accept(Examples examples) {
        return convertToId(examples.getName());
    }

    @Override
    public String accept(int examplesIndex, int exampleIndex) {
        return (examplesIndex + 1) + ";" + (examplesIndex + 1);
    }

    @Override
    public String accept(Pickle pickle) {
        return convertToId(pickle.getName());
    }
}
