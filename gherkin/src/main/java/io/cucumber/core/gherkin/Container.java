package io.cucumber.core.gherkin;

import java.util.Collection;

public interface Container<T extends Node> {

    Collection<T> children();
}
