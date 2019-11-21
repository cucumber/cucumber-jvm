package io.cucumber.core.gherkin;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface CucumberFeature extends Node, Container<Node> {

    String getKeyword();

    Optional<CucumberPickle> getPickleAt(CucumberLocation location);

    List<CucumberPickle> getPickles();

    URI getUri();

    String getSource();

}
