package io.cucumber.core.resource;

public final class ClassLoaders {

    private ClassLoaders() {

    }

    public static ClassLoader getDefaultClassLoader() {
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null) {
                return contextClassLoader;
            }
        } catch (Throwable ex) {
            /* ignore */
        }
        return ClassLoader.getSystemClassLoader();
    }

}
