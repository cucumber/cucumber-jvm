package io.cucucumber.jupiter.engine;

class ClassLoaders {

    private ClassLoaders(){

    }

    static ClassLoader getDefaultClassLoader() {
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
