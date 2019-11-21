package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.CucumberLocation;
import io.cucumber.messages.Messages;

import java.util.Objects;

final class Gherkin8CucumberLocation implements CucumberLocation {

    private final int line;
    private final int column;

    private Gherkin8CucumberLocation(int line, int column) {
        this.line = line;
        this.column = column;
    }

    static CucumberLocation from(Messages.Location location) {
        return new Gherkin8CucumberLocation(location.getLine(), location.getColumn());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gherkin8CucumberLocation that = (Gherkin8CucumberLocation) o;
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
