package io.cucumber.java8;


@FunctionalInterface
public interface ParameterDefinitionBody<T> {
    T accept(String name) throws Throwable;
}
