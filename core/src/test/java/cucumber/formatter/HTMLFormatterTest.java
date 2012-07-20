package cucumber.formatter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.tools.shell.Global;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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

    private void runFeaturesWithFormatter(File outputDir) throws IOException {
        final HTMLFormatter f = new HTMLFormatter(outputDir);
        f.uri("some.feature");
        f.done();
        f.close();
    }
}
