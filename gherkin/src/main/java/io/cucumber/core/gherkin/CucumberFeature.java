package io.cucumber.core.gherkin;

import io.cucumber.messages.Messages;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface CucumberFeature extends Node, Container<Node>, io.cucumber.plugin.event.CucumberFeature {

    String getKeyword();

    Optional<CucumberPickle> getPickleAt(CucumberLocation location);

    List<CucumberPickle> getPickles();

    URI getUri();

    String getSource();

    Iterable<Messages.Envelope> getMessages();
}
