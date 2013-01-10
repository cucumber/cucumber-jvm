package cucumber.runtime.formatter;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Utils;
import cucumber.runtime.io.UTF8OutputStreamWriter;
import gherkin.formatter.Formatter;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FormatterFactoryTest {
    private FormatterFactory fc = new FormatterFactory();

    @Test
    public void instantiates_null_formatter() {
        Formatter formatter = fc.create("null");
        assertEquals(NullFormatter.class, formatter.getClass());
    }

    @Test
    public void instantiates_junit_formatter_with_file_arg() throws IOException {
        Formatter formatter = fc.create("junit:" + File.createTempFile("cucumber", "xml"));
        assertEquals(JUnitFormatter.class, formatter.getClass());
    }

    @Test
    public void instantiates_html_formatter_with_dir_arg() throws IOException {
        Formatter formatter = fc.create("html:" + TempDir.createTempDirectory().getAbsolutePath());
        assertEquals(HTMLFormatter.class, formatter.getClass());
    }

    @Test
    public void fails_to_instantiate_html_formatter_without_dir_arg() throws IOException {
        try {
            fc.create("html");
            fail();
        } catch (CucumberException e) {
            assertEquals("You must supply an output argument to html. Like so: html:output", e.getMessage());
        }
    }

    @Test
    public void instantiates_pretty_formatter_with_file_arg() throws IOException {
        Formatter formatter = fc.create("pretty:" + Utils.toURL(TempDir.createTempFile().getAbsolutePath()));
        assertEquals(CucumberPrettyFormatter.class, formatter.getClass());
    }

    @Test
    public void instantiates_pretty_formatter_without_file_arg() {
        Formatter formatter = fc.create("pretty");
        assertEquals(CucumberPrettyFormatter.class, formatter.getClass());
    }

    @Test
    public void instantiates_usage_formatter_without_file_arg() {
        Formatter formatter = fc.create("usage");
        assertEquals(UsageFormatter.class, formatter.getClass());
    }

    @Test
    public void instantiates_usage_formatter_with_file_arg() throws IOException {
        Formatter formatter = fc.create("usage:" + TempDir.createTempFile().getAbsolutePath());
        assertEquals(UsageFormatter.class, formatter.getClass());
    }

    @Test
    public void instantiates_single_custom_appendable_formatter_with_stdout() {
        WantsAppendable formatter = (WantsAppendable) fc.create("cucumber.runtime.formatter.FormatterFactoryTest$WantsAppendable");
        assertThat(formatter.out, is(instanceOf(OutputStreamWriter.class)));
        try {
            fc.create("cucumber.runtime.formatter.FormatterFactoryTest$WantsAppendable");
            fail();
        } catch (CucumberException expected) {
            assertEquals("Only one formatter can use STDOUT. If you use more than one formatter you must specify output path with FORMAT:PATH_OR_URL", expected.getMessage());
        }
    }

    @Test
    public void instantiates_custom_appendable_formatter_with_stdout_and_file() throws IOException {
        WantsAppendable formatter = (WantsAppendable) fc.create("cucumber.runtime.formatter.FormatterFactoryTest$WantsAppendable");
        assertThat(formatter.out, is(instanceOf(OutputStreamWriter.class)));

        WantsAppendable formatter2 = (WantsAppendable) fc.create("cucumber.runtime.formatter.FormatterFactoryTest$WantsAppendable:" + TempDir.createTempFile().getAbsolutePath());
        assertEquals(UTF8OutputStreamWriter.class, formatter2.out.getClass());
    }

    @Test
    public void instantiates_custom_url_formatter() throws IOException {
        WantsUrl formatter = (WantsUrl) fc.create("cucumber.runtime.formatter.FormatterFactoryTest$WantsUrl:halp");
        assertEquals(new URL("file:halp/"), formatter.out);
    }

    @Test
    public void instantiates_custom_url_formatter_with_http() throws IOException {
        WantsUrl formatter = (WantsUrl) fc.create("cucumber.runtime.formatter.FormatterFactoryTest$WantsUrl:http://halp/");
        assertEquals(new URL("http://halp/"), formatter.out);
    }

    @Test
    public void instantiates_custom_file_formatter() throws IOException {
        WantsFile formatter = (WantsFile) fc.create("cucumber.runtime.formatter.FormatterFactoryTest$WantsFile:halp.txt");
        assertEquals(new File("halp.txt"), formatter.out);
    }

    public static class WantsAppendable extends StubFormatter {
        public final Appendable out;

        public WantsAppendable(Appendable out) {
            this.out = out;
        }
    }

    public static class WantsUrl extends StubFormatter {
        public final URL out;

        public WantsUrl(URL out) {
            this.out = out;
        }
    }

    public static class WantsFile extends StubFormatter {
        public final File out;

        public WantsFile(File out) {
            this.out = out;
        }
    }
}
