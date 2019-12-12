package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Location;
import io.cucumber.messages.Messages;

import java.util.Objects;

final class GherkinMessagesLocation implements Location {

    private final int line;
    private final int column;

    private GherkinMessagesLocation(int line, int column) {
        this.line = line;
        this.column = column;
    }

    static Location from(Messages.Location location) {
        return new GherkinMessagesLocation(location.getLine(), location.getColumn());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GherkinMessagesLocation that = (GherkinMessagesLocation) o;
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
