package io.cucumber.core.resource;

import static io.cucumber.core.exception.UnrecoverableExceptions.rethrowIfUnrecoverable;

public final class ClassLoaders {

    private ClassLoaders() {

    }

    public static ClassLoader getDefaultClassLoader() {
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null) {
                return contextClassLoader;
            }
        } catch (Throwable t) {
            rethrowIfUnrecoverable(t);
        }
        return ClassLoader.getSystemClassLoader();
    }

}
