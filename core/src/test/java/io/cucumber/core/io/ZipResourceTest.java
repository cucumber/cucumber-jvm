package io.cucumber.core.io;

import org.junit.jupiter.api.Test;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ZipResourceTest {

    @Test
    public void parses_feature_file_with_spaces_in_path() {
        JarFile jarFile = mock(JarFile.class);
        JarEntry jarEntry = new JarEntry("Feature with spaces in path.feature");
        ZipResource zipResource = new ZipResource(jarFile, jarEntry);
        assertEquals("classpath:Feature%20with%20spaces%20in%20path.feature", zipResource.getPath().toString());
    }

}
