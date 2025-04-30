package io.cucumber.core.gherkin;

import io.cucumber.plugin.event.Node;

import java.util.List;

public interface Feature extends Node.Feature {

    Pickle getPickleAt(Node node);

    List<Pickle> getPickles();

    String getSource();

    Iterable<?> getParseEvents();

}
