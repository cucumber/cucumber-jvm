package cucumber.formatter;

import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
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
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class HTMLFormatterTest {

    private File outputDir;

    @Before
    public void writeReport() throws IOException {
        outputDir = createTempDirectory();
        runFeaturesWithFormatter(asList("cucumber/formatter/HTMLFormatterTest.feature"), outputDir);
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

    private void runFeaturesWithFormatter(final List<String> featurePaths, File outputDir) throws IOException {
        final HTMLFormatter f = new HTMLFormatter(outputDir);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        final List<String> gluePaths = emptyList();
        RuntimeOptions runtimeOptions = new RuntimeOptions();
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(mock(Backend.class)), runtimeOptions);
        runtime.run(featurePaths, emptyList(), f, f);
        f.done();
        f.close();
    }

    private static File createTempDirectory() throws IOException {
        File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        temp.deleteOnExit();

        return temp;
    }
}
