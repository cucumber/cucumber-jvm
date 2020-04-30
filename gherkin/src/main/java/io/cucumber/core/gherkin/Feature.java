package io.cucumber.core.gherkin;

import io.cucumber.plugin.event.Node;

import java.net.URI;
import java.util.List;

public interface Feature extends Node.Feature {

    String getKeyWord();

    Pickle getPickleAt(Node node);

    List<Pickle> getPickles();

    URI getUri();

    String getSource();

}
