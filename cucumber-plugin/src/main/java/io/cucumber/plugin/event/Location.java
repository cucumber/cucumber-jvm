package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.util.Objects;

@API(status = API.Status.EXPERIMENTAL)
public final class Location implements Comparable<Location> {

    private final int line;
    private final int column;

    public Location(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, column);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Location location = (Location) o;
        return line == location.line &&
                column == location.column;
    }

    @Override
    public int compareTo(Location o) {
        Objects.requireNonNull(o);
        int c = Integer.compare(line, o.line);
        if (c != 0) {
            return c;
        }
        return Integer.compare(column, o.column);
    }
}
