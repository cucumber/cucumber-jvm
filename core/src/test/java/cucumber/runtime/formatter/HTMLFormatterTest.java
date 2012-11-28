package cucumber.runtime.formatter;

import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.Tag;
import gherkin.util.FixJava;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.tools.shell.Global;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HTMLFormatterTest {

    private File outputDir;

    @Before
    public void writeReport() throws IOException {
        outputDir = TempDir.createTempDirectory();
        runFeaturesWithFormatter(outputDir);
    }

    @Test
    public void writes_index_html() throws IOException {
        File indexHtml = new File(outputDir, "index.html");
        Document document = Jsoup.parse(indexHtml, "UTF-8");
        Element reportElement = document.body().getElementsByClass("cucumber-report").first();
        assertEquals("", reportElement.text());
    }

    @Test
    public void writes_valid_report_js() throws IOException {
        File reportJs = new File(outputDir, "report.js");
        Context cx = Context.enter();
        Global scope = new Global(cx);
        try {
            cx.evaluateReader(scope, new FileReader(reportJs), reportJs.getAbsolutePath(), 1, null);
            fail("Should have failed");
        } catch (EcmaError expected) {
            assertTrue(expected.getMessage().startsWith("ReferenceError: \"document\" is not defined."));
        }
    }

    @Test
    public void includes_uri() throws FileNotFoundException {
        String reportJs = FixJava.readReader(new FileReader(new File(outputDir, "report.js")));
        assertContains("formatter.uri(\"some\\\\windows\\\\path\\\\some.feature\");", reportJs);
    }

    @Test
    public void included_embedding() throws FileNotFoundException {
        String reportJs = FixJava.readReader(new FileReader(new File(outputDir, "report.js")));
        assertContains("formatter.embedding(\"image/png\", \"embedded0.png\");", reportJs);
    }

    private void assertContains(String substring, String string) {
        if (string.indexOf(substring) == -1) {
            fail(String.format("[%s] not contained in [%s]", substring, string));
        }
    }

    private void runFeaturesWithFormatter(File outputDir) throws IOException {
        final HTMLFormatter f = new HTMLFormatter(outputDir);
        f.uri("some\\windows\\path\\some.feature");
        f.scenario(new Scenario(Collections.<Comment>emptyList(), Collections.<Tag>emptyList(), "Scenario", "some cukes", "", 10, "id"));
        f.embedding("image/png", "fakedata".getBytes("US-ASCII"));
        f.done();
        f.close();
    }
}
