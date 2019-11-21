package io.cucumber.core.feature;

import java.util.stream.Stream;

public interface Container<T extends Located & Named> {

    Stream<T> children();
}
