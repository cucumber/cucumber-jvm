package io.cucumber.core.gherkin5;

import gherkin.pickles.PickleLocation;
import io.cucumber.core.gherkin.Location;

import java.util.Objects;

final class Gherkin5Location implements Location {

    private final int line;
    private final int column;

    private Gherkin5Location(int line, int column) {
        this.line = line;
        this.column = column;
    }

    static Location from(PickleLocation location) {
        return new Gherkin5Location(location.getLine(), location.getColumn());
    }

    public static Location from(gherkin.ast.Location location) {
        return new Gherkin5Location(location.getLine(), location.getColumn());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gherkin5Location that = (Gherkin5Location) o;
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
