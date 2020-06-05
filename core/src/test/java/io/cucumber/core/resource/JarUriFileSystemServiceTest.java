package io.cucumber.core.resource;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;

import static io.cucumber.core.resource.ClasspathSupport.getUrisForPackage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JarUriFileSystemServiceTest {

    @Test
    void supports() {
        assertTrue(JarUriFileSystemService.supports(URI.create("jar:file:/example.jar!com/example/app")));
        assertTrue(JarUriFileSystemService.supports(URI.create("file:/example.jar")));
    }

    @Test
    void canOpenMultipleConcurrently() throws IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URI first = getUrisForPackage(classLoader, "io.cucumber").stream()
                .filter(JarUriFileSystemService::supports)
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        CloseablePath path1 = JarUriFileSystemService.open(first);
        FileSystem fileSystem1 = path1.getPath().getFileSystem();

        CloseablePath path2 = JarUriFileSystemService.open(first);
        FileSystem fileSystem2 = path2.getPath().getFileSystem();

        assertThat(fileSystem1, is(fileSystem2));

        path1.close();
        assertTrue(fileSystem1.isOpen());
        assertTrue(fileSystem2.isOpen());

        path2.close();
        assertFalse(fileSystem1.isOpen());
        assertFalse(fileSystem2.isOpen());
    }

}
