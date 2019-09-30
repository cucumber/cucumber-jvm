package io.cucumber.java8;

import java.util.Map;

@FunctionalInterface
public interface DataTableEntryDefinitionBody<T> {
    T accept(Map<String, String> map) throws Throwable;
}
