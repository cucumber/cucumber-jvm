package io.cucumber.core.io;

import java.net.URI;
import java.util.Collection;

public interface ClassFinder {
    <T> Collection<Class<? extends T>> getDescendants(Class<T> parentType, URI packageName);

    <T> Class<? extends T> loadClass(String className) throws ClassNotFoundException;
}
