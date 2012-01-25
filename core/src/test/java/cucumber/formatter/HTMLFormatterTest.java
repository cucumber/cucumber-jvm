package cucumber.formatter;

import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
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
    public void writes_proper_html() throws IOException {
        File dir = createTempDirectory();
        HTMLFormatter f = new HTMLFormatter(dir);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        List<CucumberFeature> features = CucumberFeature.load(resourceLoader, asList("cucumber/formatter/HTMLFormatterTest.feature"), emptyList());
        List<String> gluePaths = emptyList();
        Runtime runtime = new Runtime(resourceLoader, gluePaths, classLoader, asList(mock(Backend.class)), false);
        runtime.run(features.get(0), f, f);
        f.done();
        f.close();

        // Let's verify that the JS we wrote parses nicely

        Context cx = Context.enter();
        Global scope = new Global(cx);
        File report = new File(dir, "report.js");
        try {
            cx.evaluateReader(scope, new FileReader(report), report.getAbsolutePath(), 1, null);
            fail("Should have failed");
        } catch (EcmaError expected) {
            assertTrue(expected.getMessage().startsWith("ReferenceError: \"document\" is not defined."));
        }
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

    private class CucumberHTML {
        public void undefined() {

        }

        public void DOMFormatter() {

        }
    }
}
