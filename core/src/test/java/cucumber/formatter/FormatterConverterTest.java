package cucumber.formatter;

import cucumber.runtime.CucumberException;
import gherkin.formatter.Formatter;
import org.junit.Test;

import java.io.IOException;

import static cucumber.formatter.TempDir.createTempDirectory;
import static cucumber.formatter.TempDir.createTempFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FormatterConverterTest {
    private FormatterConverter fc = new FormatterConverter();

    @Test
    public void instantiates_junit_formatter_with_file_arg() {
        Formatter formatter = fc.convert("junit:some_file.xml");
        assertEquals(JUnitFormatter.class, formatter.getClass());
    }

    @Test
    public void instantiates_html_formatter_with_dir_arg() throws IOException {
        Formatter formatter = fc.convert("html:" + createTempDirectory().getAbsolutePath());
        assertEquals(HTMLFormatter.class, formatter.getClass());
    }

    @Test
    public void fails_to_instantiate_html_formatter_without_dir_arg() throws IOException {
        try {
            fc.convert("html");
            fail();            
        } catch(CucumberException e) {
            assertEquals("You must supply an output argument to html. Like so: html:output", e.getMessage());
        }
    }

    @Test
    public void instantiates_pretty_formatter_with_file_arg() throws IOException {
        Formatter formatter = fc.convert("pretty:" + createTempFile().getAbsolutePath());
        assertEquals(CucumberPrettyFormatter.class, formatter.getClass());
    }

    @Test
    public void instantiates_pretty_formatter_without_file_arg() {
        Formatter formatter = fc.convert("pretty");
        assertEquals(CucumberPrettyFormatter.class, formatter.getClass());
    }
}
