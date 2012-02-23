package cucumber.runtime;

import com.beust.jcommander.JCommander;
import cucumber.formatter.HTMLFormatter;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertEquals;

public class RuntimeOptionsTest {
    @Test
    public void assigns_glue() {
        RuntimeOptions options = new RuntimeOptions("--glue", "somewhere");
        assertEquals("somewhere", options.glue);
    }

    @Test
    public void assigns_dotcucumber() {
        RuntimeOptions options = new RuntimeOptions("--dotcucumber", "somewhere", "--glue", "somewhere");
        assertEquals(new File("somewhere"), options.dotCucumber);
    }

    @Test
    public void creates_formatter() {
        RuntimeOptions options = new RuntimeOptions("--format", "html:some/dir", "--glue", "somewhere");
        assertEquals(HTMLFormatter.class, options.formatters.get(0).getClass());
    }
}
