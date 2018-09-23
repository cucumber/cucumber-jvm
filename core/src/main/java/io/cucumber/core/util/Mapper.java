package io.cucumber.core.util;

public interface Mapper<T, R> {
    R map(T o);
}
