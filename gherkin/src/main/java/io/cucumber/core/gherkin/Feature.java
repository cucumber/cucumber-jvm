package io.cucumber.core.gherkin;

import io.cucumber.plugin.event.Node;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface Feature extends Node.Feature {

    Optional<Pickle> getPickleAt(Node node);

    List<Pickle> getPickles();

    URI getUri();

    String getSource();

}
