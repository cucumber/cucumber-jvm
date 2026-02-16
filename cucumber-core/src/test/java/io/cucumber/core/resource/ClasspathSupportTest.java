package io.cucumber.core.resource;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;

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
    void determinePackageName() {
        Path baseDir = Path.of("path", "to", "com", "example", "app");
        String basePackageName = "com.example.app";
        Path classFile = Path.of("path", "to", "com", "example", "app", "App.class");
        String packageName = ClasspathSupport.determinePackageName(baseDir, basePackageName, classFile);
        assertEquals("com.example.app", packageName);
    }

    @Test
    void determinePackageNameFromRootPackage() {
        Path baseDir = Path.of("path", "to");
        String basePackageName = "";
        Path classFile = Path.of("path", "to", "com", "example", "app", "App.class");
        String packageName = ClasspathSupport.determinePackageName(baseDir, basePackageName, classFile);
        assertEquals("com.example.app", packageName);
    }

    @Test
    void determinePackageNameFromComPackage() {
        Path baseDir = Path.of("path", "to", "com");
        String basePackageName = "com";
        Path classFile = Path.of("path", "to", "com", "example", "app", "App.class");
        String packageName = ClasspathSupport.determinePackageName(baseDir, basePackageName, classFile);
        assertEquals("com.example.app", packageName);
    }

    @Test
    void determineFullyQualifiedClassName() {
        Path baseDir = Path.of("path", "to", "com", "example", "app");
        String basePackageName = "com.example.app";
        Path classFile = Path.of("path", "to", "com", "example", "app", "App.class");
        String fqn = ClasspathSupport.determineFullyQualifiedClassName(baseDir, basePackageName, classFile);
        assertEquals("com.example.app.App", fqn);
    }

    @Test
    void determineFullyQualifiedClassNameFromRootPackage() {
        Path baseDir = Path.of("path", "to");
        String basePackageName = "";
        Path classFile = Path.of("path", "to", "com", "example", "app", "App.class");
        String fqn = ClasspathSupport.determineFullyQualifiedClassName(baseDir, basePackageName, classFile);
        assertEquals("com.example.app.App", fqn);
    }

    @Test
    void determineFullyQualifiedClassNameFromComPackage() {
        Path baseDir = Path.of("path", "to", "com");
        String basePackageName = "com";
        Path classFile = Path.of("path", "to", "com", "example", "app", "App.class");
        String fqn = ClasspathSupport.determineFullyQualifiedClassName(baseDir, basePackageName, classFile);
        assertEquals("com.example.app.App", fqn);
    }

    @Test
    void determineFullyQualifiedResourceName() {
        Path baseDir = Path.of("path", "to", "com", "example", "app");
        String basePackageName = "com/example/app";
        Path resourceFile = Path.of("path", "to", "com", "example", "app", "app.feature");
        URI fqn = ClasspathSupport.determineClasspathResourceUri(baseDir, basePackageName, resourceFile);
        assertEquals(URI.create("classpath:com/example/app/app.feature"), fqn);
    }

    @Test
    void determineFullyQualifiedResourceNameFromRootPackage() {
        Path baseDir = Path.of("path", "to");
        String basePackageName = "";
        Path resourceFile = Path.of("path", "to", "com", "example", "app", "app.feature");
        URI fqn = ClasspathSupport.determineClasspathResourceUri(baseDir, basePackageName, resourceFile);
        assertEquals(URI.create("classpath:com/example/app/app.feature"), fqn);
    }

    @Test
    void determineFullyQualifiedResourceNameFromComPackage() {
        Path baseDir = Path.of("path", "to", "com");
        String basePackageName = "com";
        Path resourceFile = Path.of("path", "to", "com", "example", "app", "app.feature");
        URI fqn = ClasspathSupport.determineClasspathResourceUri(baseDir, basePackageName, resourceFile);
        assertEquals(URI.create("classpath:com/example/app/app.feature"), fqn);
    }

}
