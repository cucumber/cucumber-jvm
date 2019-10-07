package io.cucumber.java8;

import java.util.List;

@FunctionalInterface
public interface DataTableRowDefinitionBody<T> {
    T accept(List<String> row) throws Throwable;
}
