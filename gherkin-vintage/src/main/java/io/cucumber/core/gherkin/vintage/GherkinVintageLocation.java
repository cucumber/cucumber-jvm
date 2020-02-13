package io.cucumber.core.gherkin.vintage;

import gherkin.pickles.PickleLocation;
import io.cucumber.plugin.event.Location;

import java.util.Objects;

final class GherkinVintageLocation{

    static Location from(PickleLocation location) {
        return new Location(location.getLine(), location.getColumn());
    }

    public static Location from(gherkin.ast.Location location) {
        return new Location(location.getLine(), location.getColumn());
    }

}
