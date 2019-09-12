package io.cucumber.java8;

import java.util.Map;

@FunctionalInterface
public interface DataTableDefinitionBody<T> {

    T accept(Map<String, String> map) throws Throwable;

}
