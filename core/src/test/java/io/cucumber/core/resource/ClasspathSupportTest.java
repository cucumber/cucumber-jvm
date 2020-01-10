package io.cucumber.core.resource;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClasspathSupportTest {

    @Test
    void packageName() {
        URI classpathResourceUri = URI.create("classpath:com/example");
        String packageName = ClasspathSupport.packageName(classpathResourceUri);
        assertEquals("com.example", packageName);

    }

    @Test
    void packageNameOfResource() {
        String packageName = ClasspathSupport.packageNameOfResource("com/example/app.feature");
        assertEquals("com.example", packageName);
    }

    @Test
    void determineFullyQualifiedClassName() {
        Path baseDir = Paths.get("path", "to", "com", "example", "app");
        String basePackageName = "com.example";
        Path classFile = Paths.get("path", "to", "com", "example", "app", "App.class");
        String fqn = ClasspathSupport.determineFullyQualifiedClassName(baseDir, basePackageName, classFile);
        assertEquals("com.example.App", fqn);
    }
}