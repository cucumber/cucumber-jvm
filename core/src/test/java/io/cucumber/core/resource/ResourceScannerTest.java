package io.cucumber.core.resource;

import io.cucumber.core.exception.CucumberException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.net.URI;
import java.util.List;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceScannerTest {

    private final ResourceScanner<URI> resourceScanner = new ResourceScanner<>(
        ResourceScannerTest.class::getClassLoader,
        path -> path.getFileName().toString().endsWith("resource.txt"),
        resource -> of(resource.getUri()));

    @Test
    void scanForResourcesInClasspathRoot() {
        URI classpathRoot = new File("src/test/resources/io/cucumber/core/resource/test").toURI();
        List<URI> resources = resourceScanner.scanForResourcesInClasspathRoot(classpathRoot, aPackage -> true);
        assertThat(resources, containsInAnyOrder(
            URI.create("classpath:resource.txt"),
            URI.create("classpath:other-resource.txt"),
            URI.create("classpath:spaces%20in%20name%20resource.txt")));
    }

    @Test
    void scanForResourcesInClasspathRootJar() {
        URI classpathRoot = new File("src/test/resources/io/cucumber/core/resource/test/jar-resource.jar").toURI();
        List<URI> resources = resourceScanner.scanForResourcesInClasspathRoot(classpathRoot, aPackage -> true);
        assertThat(resources, containsInAnyOrder(
            URI.create("classpath:jar-resource.txt"),
            URI.create("classpath:com/example/package-jar-resource.txt")));
    }

    @Test
    void scanForResourcesInClasspathRootWithPackage() {
        URI classpathRoot = new File("src/test/resources").toURI();
        List<URI> resources = resourceScanner.scanForResourcesInClasspathRoot(classpathRoot, aPackage -> true);
        assertThat(resources, containsInAnyOrder(
            URI.create("classpath:io/cucumber/core/resource/test/resource.txt"),
            URI.create("classpath:io/cucumber/core/resource/test/other-resource.txt"),
            URI.create("classpath:io/cucumber/core/resource/test/spaces%20in%20name%20resource.txt")));
    }

    @Test
    void scanForResourcesInPackage() {
        String basePackageName = "io.cucumber.core.resource.test";
        List<URI> resources = resourceScanner.scanForResourcesInPackage(basePackageName, aPackage -> true);
        assertThat(resources, containsInAnyOrder(
            URI.create("classpath:io/cucumber/core/resource/test/resource.txt"),
            URI.create("classpath:io/cucumber/core/resource/test/other-resource.txt"),
            URI.create("classpath:io/cucumber/core/resource/test/spaces%20in%20name%20resource.txt")));
    }

    @Test
    void scanForResourcesInSubPackage() {
        String basePackageName = "io.cucumber.core.resource";
        List<URI> resources = resourceScanner.scanForResourcesInPackage(basePackageName, aPackage -> true);
        assertThat(resources, containsInAnyOrder(
            URI.create("classpath:io/cucumber/core/resource/test/resource.txt"),
            URI.create("classpath:io/cucumber/core/resource/test/other-resource.txt"),
            URI.create("classpath:io/cucumber/core/resource/test/spaces%20in%20name%20resource.txt")));
    }

    @Test
    void scanForClasspathResource() {
        String resourceName = "io/cucumber/core/resource/test/resource.txt";
        List<URI> resources = resourceScanner.scanForClasspathResource(resourceName, aPackage -> true);
        assertThat(resources, contains(URI.create("classpath:io/cucumber/core/resource/test/resource.txt")));
    }

    @Test
    void scanForClasspathResourceWithSpaces() {
        String resourceName = "io/cucumber/core/resource/test/spaces in name resource.txt";
        List<URI> resources = resourceScanner.scanForClasspathResource(resourceName, aPackage -> true);
        assertThat(resources,
            contains(URI.create("classpath:io/cucumber/core/resource/test/spaces%20in%20name%20resource.txt")));
    }

    @Test
    void scanForClasspathPackageResource() {
        String resourceName = "io/cucumber/core/resource";
        List<URI> resources = resourceScanner.scanForClasspathResource(resourceName, aPackage -> true);
        assertThat(resources, containsInAnyOrder(
            URI.create("classpath:io/cucumber/core/resource/test/resource.txt"),
            URI.create("classpath:io/cucumber/core/resource/test/other-resource.txt"),
            URI.create("classpath:io/cucumber/core/resource/test/spaces%20in%20name%20resource.txt")));
    }

    @Test
    void scanForResourcesPath() {
        File file = new File("src/test/resources/io/cucumber/core/resource/test/resource.txt");
        List<URI> resources = resourceScanner.scanForResourcesPath(file.toPath());
        assertThat(resources, contains(file.toURI()));
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS,
            disabledReason = "Only works if repository is explicitly cloned activated symlinks and " +
                    "developer mode in windows is activated")
    void scanForResourcesPathSymlink() {
        File file = new File("src/test/resource-symlink/test/resource.txt");
        List<URI> resources = resourceScanner.scanForResourcesPath(file.toPath());
        assertThat(resources, contains(file.toURI()));
    }

    @Test
    void scanForResourcesDirectory() {
        File file = new File("src/test/resources/io/cucumber/core/resource");
        List<URI> resources = resourceScanner.scanForResourcesPath(file.toPath());
        assertThat(resources, containsInAnyOrder(
            new File("src/test/resources/io/cucumber/core/resource/test/resource.txt").toURI(),
            new File("src/test/resources/io/cucumber/core/resource/test/other-resource.txt").toURI(),
            new File("src/test/resources/io/cucumber/core/resource/test/spaces in name resource.txt").toURI()));
    }

    @Test
    @DisabledOnOs(value = OS.WINDOWS,
            disabledReason = "Only works if repository is explicitly cloned activated symlinks and " +
                    "developer mode in windows is activated")
    void scanForResourcesDirectorySymlink() {
        File file = new File("src/test/resource-symlink");
        List<URI> resources = resourceScanner.scanForResourcesPath(file.toPath());
        assertThat(resources, containsInAnyOrder(
            new File("src/test/resource-symlink/test/resource.txt").toURI(),
            new File("src/test/resource-symlink/test/other-resource.txt").toURI(),
            new File("src/test/resource-symlink/test/spaces in name resource.txt").toURI()));
    }

    @Test
    void scanForResourcesFileUri() {
        File file = new File("src/test/resources/io/cucumber/core/resource/test/resource.txt");
        List<URI> resources = resourceScanner.scanForResourcesUri(file.toURI());
        assertThat(resources, contains(file.toURI()));
    }

    @Test
    void scanForResourcesJarUri() {
        URI jarFileUri = new File("src/test/resources/io/cucumber/core/resource/test/jar-resource.jar").toURI();
        URI resourceUri = URI
                .create("jar:file://" + jarFileUri.getSchemeSpecificPart() + "!/com/example/package-jar-resource.txt");
        List<URI> resources = resourceScanner.scanForResourcesUri(resourceUri);
        assertThat(resources, contains(resourceUri));
    }

    @Test
    void scanForResourcesNestedJarUri() {
        URI jarFileUri = new File("src/test/resources/io/cucumber/core/resource/test/spring-resource.jar").toURI();
        URI resourceUri = URI.create("jar:file://" + jarFileUri.getSchemeSpecificPart()
                + "!/BOOT-INF/lib/jar-resource.jar!/com/example/package-jar-resource.txt");

        CucumberException exception = assertThrows(
            CucumberException.class,
            () -> resourceScanner.scanForResourcesUri(resourceUri));
        assertThat(exception.getMessage(),
            containsString("Cucumber currently doesn't support classpath scanning in nested jars."));

    }

    @Test
    void scanForResourcesNestedJarUriUnPackaged() {
        URI jarFileUri = new File("src/test/resources/io/cucumber/core/resource/test/spring-resource.jar").toURI();
        URI resourceUri = URI
                .create("jar:file://" + jarFileUri.getSchemeSpecificPart() + "!/BOOT-INF/classes!/com/example/");

        List<URI> resources = resourceScanner.scanForResourcesUri(resourceUri);
        assertThat(resources, containsInAnyOrder(
            URI.create(
                "jar:file://" + jarFileUri.getSchemeSpecificPart() + "!/BOOT-INF/classes/com/example/resource.txt")));
    }

    @Test
    void scanForResourcesDirectoryUri() {
        File file = new File("src/test/resources/io/cucumber/core/resource");
        List<URI> resources = resourceScanner.scanForResourcesUri(file.toURI());
        assertThat(resources, containsInAnyOrder(
            new File("src/test/resources/io/cucumber/core/resource/test/resource.txt").toURI(),
            new File("src/test/resources/io/cucumber/core/resource/test/other-resource.txt").toURI(),
            new File("src/test/resources/io/cucumber/core/resource/test/spaces in name resource.txt").toURI()));
    }

    @Test
    void scanForResourcesClasspathUri() {
        URI uri = URI.create("classpath:io/cucumber/core/resource/test/resource.txt");
        List<URI> resources = resourceScanner.scanForResourcesUri(uri);
        assertThat(resources, contains(uri));
    }

    @Test
    void scanForResourcesClasspathPackageUri() {
        URI uri = URI.create("classpath:io/cucumber/core/resource");
        List<URI> resources = resourceScanner.scanForResourcesUri(uri);
        assertThat(resources, containsInAnyOrder(
            URI.create("classpath:io/cucumber/core/resource/test/resource.txt"),
            URI.create("classpath:io/cucumber/core/resource/test/other-resource.txt"),
            URI.create("classpath:io/cucumber/core/resource/test/spaces%20in%20name%20resource.txt")));
    }

}
