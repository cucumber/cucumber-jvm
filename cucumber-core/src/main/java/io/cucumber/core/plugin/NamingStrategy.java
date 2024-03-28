package io.cucumber.core.plugin;

import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.TableRow;

public interface NamingStrategy {
    String name(GherkinAstNodes nodes, Feature element);

    String name(GherkinAstNodes nodes, Rule element);

    String name(GherkinAstNodes nodes, Scenario element);

    String name(GherkinAstNodes nodes, Examples element);

    String name(GherkinAstNodes nodes, TableRow element);

    String name(GherkinAstNodes nodes, Pickle pickle);
}
