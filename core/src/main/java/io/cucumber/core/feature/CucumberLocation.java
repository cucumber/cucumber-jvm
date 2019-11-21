package io.cucumber.core.feature;

import gherkin.ast.Location;
import gherkin.pickles.PickleLocation;

import java.util.Objects;

public final class CucumberLocation {
    private final int line;
    private final int column;

    private CucumberLocation(int line, int column) {
        this.line = line;
        this.column = column;
    }

    static CucumberLocation from(Location location) {
        return new CucumberLocation(location.getLine(), location.getColumn());
    }

    static CucumberLocation from(PickleLocation location) {
        return new CucumberLocation(location.getLine(), location.getColumn());
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CucumberLocation that = (CucumberLocation) o;
        return line == that.line &&
            column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, column);
    }
}
