package io.cucumber.datatable;

import java.util.Objects;

class Place {

    final String name;
    final int indexOfPlace;

    Place(String name) {
        this(name, -1);
    }

    Place(String name, int indexOfPlace) {
        this.name = name;
        this.indexOfPlace = indexOfPlace;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Place place))
            return false;
        return indexOfPlace == place.indexOfPlace && Objects.equals(name, place.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, indexOfPlace);
    }

    @Override
    public String toString() {
        return "Place{" +
                "name='" + name + '\'' +
                ", indexOfPlace=" + indexOfPlace +
                '}';
    }
}
