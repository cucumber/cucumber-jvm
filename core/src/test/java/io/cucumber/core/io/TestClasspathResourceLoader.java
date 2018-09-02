package io.cucumber.core.io;


public class TestClasspathResourceLoader {

    public static ResourceLoader create(ClassLoader classLoader) {
        return new ClasspathResourceLoader(classLoader);
    }

}