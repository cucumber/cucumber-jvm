package cucumber.runtime.formatter;

import cucumber.runtime.Utils;
import cucumber.util.FixJava;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.tools.shell.Global;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HTMLFormatterTest {

    private URL outputDir;

    @Before
    public void writeReport() throws IOException {
        outputDir = Utils.toURL(TempDir.createTempDirectory().getAbsolutePath());
        runFeaturesWithFormatter(outputDir);
    }

    @Test @org.junit.Ignore
    public void writes_index_html() throws IOException {
        URL indexHtml = new URL(outputDir, "index.html");
        Document document = Jsoup.parse(new File(indexHtml.getFile()), "UTF-8");
        Element reportElement = document.body().getElementsByClass("cucumber-report").first();
        assertEquals("", reportElement.text());
    }

    @Test @org.junit.Ignore
    public void writes_valid_report_js() throws IOException {
        URL reportJs = new URL(outputDir, "report.js");
        Context cx = Context.enter();
        Global scope = new Global(cx);
        try {
            cx.evaluateReader(scope, new InputStreamReader(reportJs.openStream(), "UTF-8"), reportJs.getFile(), 1, null);
            fail("Should have failed");
        } catch (EcmaError expected) {
            assertTrue(expected.getMessage().startsWith("ReferenceError: \"document\" is not defined."));
        }
    }

    @Test @org.junit.Ignore
    public void includes_uri() throws IOException {
        String reportJs = FixJava.readReader(new InputStreamReader(new URL(outputDir, "report.js").openStream(), "UTF-8"));
        assertContains("formatter.uri(\"some\\\\windows\\\\path\\\\some.feature\");", reportJs);
    }

    @Test @org.junit.Ignore
    public void included_embedding() throws IOException {
        String reportJs = FixJava.readReader(new InputStreamReader(new URL(outputDir, "report.js").openStream(), "UTF-8"));
        assertContains("formatter.embedding(\"image/png\", \"embedded0.png\");", reportJs);
        assertContains("formatter.embedding(\"text/plain\", \"dodgy stack trace here\");", reportJs);
    }

    private void assertContains(String substring, String string) {
        if (string.indexOf(substring) == -1) {
            fail(String.format("[%s] not contained in [%s]", substring, string));
        }
    }

    private void runFeaturesWithFormatter(URL outputDir) throws IOException {
        final HTMLFormatter f = new HTMLFormatter(outputDir);
//        f.uri("some\\windows\\path\\some.feature");
        //f.scenario(new Scenario(Collections.<Comment>emptyList(), Collections.<Tag>emptyList(), "Scenario", "some cukes", "", 10, "id"));
//        f.embedding("image/png", "fakedata".getBytes("US-ASCII"));
//        f.embedding("text/plain", "dodgy stack trace here".getBytes("US-ASCII"));
//        f.done();
        f.close();
    }
}
