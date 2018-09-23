package io.cucumber.core.io;

import java.util.Collection;

public interface ClassFinder {
    <T> Collection<Class<? extends T>> getDescendants(Class<T> parentType, String packageName);

    <T> Class<? extends T> loadClass(String className) throws ClassNotFoundException;
}
