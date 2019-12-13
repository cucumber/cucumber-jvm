package io.cucumber.core.gherkin;

import java.util.stream.Stream;

public interface Container<T> {

    Stream<T> children();
}
