package io.cucumber.junit.platform.engine;

import io.cucumber.plugin.event.Node;

interface NamingStrategy {

    String name(Node node);

}
