package cucumber.runtime.io;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ZipResourceTest {

    @Test
    public void parses_feature_file_with_spaces_in_path(){
        JarFile jarFile = mock(JarFile.class);
        JarEntry jarEntry = new JarEntry("Feature with spaces in path.feature");
        ZipResource zipResource = new ZipResource(jarFile, jarEntry);
        assertEquals("classpath:Feature%20with%20spaces%20in%20path.feature", zipResource.getPath().toString());
    }
}
