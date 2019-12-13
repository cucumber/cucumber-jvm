package io.cucumber.core.gherkin;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface Feature extends Node, Container<Node> {

    String getKeyword();

    Optional<Pickle> getPickleAt(Located located);

    List<Pickle> getPickles();

    URI getUri();

    String getSource();

}
