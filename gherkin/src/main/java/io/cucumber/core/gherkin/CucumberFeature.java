package io.cucumber.core.gherkin;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface CucumberFeature extends Node, Container<Node>, io.cucumber.plugin.event.CucumberFeature {

    String getKeyword();

    Optional<? extends CucumberPickle> getPickleAt(CucumberLocation location);

    List<? extends CucumberPickle> getPickles();

    URI getUri();

    String getSource();

}
