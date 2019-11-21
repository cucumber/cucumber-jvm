package io.cucumber.core.gherkin5;

import gherkin.ast.Location;
import gherkin.pickles.PickleLocation;
import io.cucumber.core.gherkin.CucumberLocation;

import java.util.Objects;

final class Gherkin5CucumberLocation implements CucumberLocation {

    private final int line;
    private final int column;

    private Gherkin5CucumberLocation(int line, int column) {
        this.line = line;
        this.column = column;
    }

    static CucumberLocation from(PickleLocation location) {
        return new Gherkin5CucumberLocation(location.getLine(), location.getColumn());
    }

    public static CucumberLocation from(Location location) {
        return new Gherkin5CucumberLocation(location.getLine(), location.getColumn());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gherkin5CucumberLocation that = (Gherkin5CucumberLocation) o;
        return line == that.line &&
            column == that.column;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, column);
    }
}
