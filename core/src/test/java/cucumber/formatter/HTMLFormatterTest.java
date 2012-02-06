package cucumber.formatter;

import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class HTMLFormatterTest {

    @Test
    public void oneFeatureProducesValidJavascript() throws IOException {
        final File report = createFormatterJsReport(asList("cucumber/formatter/HTMLFormatterTest.feature"));

        Context cx = Context.enter();
        Global scope = new Global(cx);
        try {
            cx.evaluateReader(scope, new FileReader(report), report.getAbsolutePath(), 1, null);
            fail("Should have failed");
        } catch (EcmaError expected) {
            assertTrue(expected.getMessage().startsWith("ReferenceError: \"document\" is not defined."));
        }
    }

    private File createFormatterJsReport(final List<String> featurePaths) throws IOException {
        final File outputDir = runFeaturesWithFormatter(featurePaths);
        return new File(outputDir, "report.js");
    }

    private File runFeaturesWithFormatter(final List<String> featurePaths) throws IOException {
        final File dir = createTempDirectory();
        final HTMLFormatter f = new HTMLFormatter(dir);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        final List<String> gluePaths = emptyList();
        final Runtime runtime = new Runtime(resourceLoader, gluePaths, classLoader, asList(mock(Backend.class)), false);
        runtime.run(featurePaths, emptyList(), f, f);
        f.done();
        f.close();
        return dir;
    }

    private static File createTempDirectory() throws IOException {
        File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return temp;
    }
}
