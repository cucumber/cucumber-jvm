package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.plugin.event.Node;

interface NamingStrategy {

    String name(Node node);

    String nameExample(Node node, Pickle pickle);
}
