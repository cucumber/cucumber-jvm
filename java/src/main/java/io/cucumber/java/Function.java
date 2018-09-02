package io.cucumber.java;

public interface Function<T, R> {

    R apply(T t);

}
