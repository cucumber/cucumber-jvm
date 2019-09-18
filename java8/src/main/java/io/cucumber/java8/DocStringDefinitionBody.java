package io.cucumber.java8;

@FunctionalInterface
public interface DocStringDefinitionBody<T> {
    T accept(String docString) throws Throwable;
}
