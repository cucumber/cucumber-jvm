package cucumber.runtime;

import cucumber.resources.FilePathExtractor;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

public class FilePathExtractorTest {

    /** Test for Issue #17*/
    @Test
    public void replacesUrlEncodedBlanksWithActualBlanks() {
        String pathToJar = FilePathExtractor.filePath("jar:file:/C:/path%20with%20blanks/actual.jar!/content.file");
        assertThat(pathToJar, not(containsString("%20")));
    }
}
