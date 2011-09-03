package cucumber.runtime;

import cucumber.resources.FilePathExtractor;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

/**
 * Tests for Issue #17
 */
public class FilePathExtractorTest {
    @Test
    public void replacesUrlEncodedBlanksWithActualBlanks() throws UnsupportedEncodingException {
        String pathToJar = new FilePathExtractor().filePath("jar:file:/C:/path%20with%20blanks/actual.jar!/content.file");
        assertEquals("C:/path with blanks/actual.jar", pathToJar);
    }
}
