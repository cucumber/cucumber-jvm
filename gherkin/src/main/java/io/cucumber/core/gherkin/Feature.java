package io.cucumber.core.gherkin;

import java.net.URI;
import java.util.List;

public interface Feature extends Node, Container<Node> {

    String getKeyword();

    Pickle getPickleAt(Located located);

    List<Pickle> getPickles();

    URI getUri();

    String getSource();

}
