package io.cucumber.core.gherkin;

import io.cucumber.core.gherkin.Located;
import io.cucumber.core.gherkin.Named;

import java.util.stream.Stream;

public interface Container<T extends Located & Named> {

    Stream<T> children();
}
