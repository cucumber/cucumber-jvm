package cucumber.runtime;

import cucumber.resources.Decoder;
import cucumber.resources.FilePathExtractor;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for Issue #17
 */
public class FilePathExtractorTest {
    public static final String anyString = "";

    @Test
    public void replacesUrlEncodedBlanksWithActualBlanks() {
        String pathToJar = new FilePathExtractor().filePath("jar:file:/C:/path%20with%20blanks/actual.jar!/content.file");
        assertThat(pathToJar, not(containsString("%20")));
    }

    @Test(expected = CucumberException.class)
    public void forwardsExceptionsAsCucumberExceptions() throws Exception {
        Decoder decoder = mock(Decoder.class);
        when(decoder.decode(anyString())).thenThrow(new UnsupportedEncodingException());
        new FilePathExtractor(decoder).resolveEncodedBlanksInPath(anyString);
    }
}
