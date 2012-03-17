package cucumber.formatter;

import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class JUnitFormatterTest {

    @Test
    public void featureSimpleTest() throws Exception {
        File report = runFeaturesWithJunitFormatter(asList("cucumber/formatter/JUnitFormatterTest_1.feature"));
        assertXmlEqual("cucumber/formatter/JUnitFormatterTest_1.report.xml", report);
    }

    @Test
    public void featureWithBackgroundTest() throws Exception {
        File report = runFeaturesWithJunitFormatter(asList("cucumber/formatter/JUnitFormatterTest_2.feature"));
        assertXmlEqual("cucumber/formatter/JUnitFormatterTest_2.report.xml", report);
    }

    @Test
    public void featureWithOutlineTest() throws Exception {
        File report = runFeaturesWithJunitFormatter(asList("cucumber/formatter/JUnitFormatterTest_3.feature"));
        assertXmlEqual("cucumber/formatter/JUnitFormatterTest_3.report.xml", report);
    }

    private File runFeaturesWithJunitFormatter(final List<String> featurePaths) throws IOException {
        File report = File.createTempFile("cucumber-jvm-junit", "xml");
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);

        List<String> args = new ArrayList<String>();
        args.add("--format");
        args.add("junit:" + report.getAbsolutePath());
        args.addAll(featurePaths);

        RuntimeOptions runtimeOptions = new RuntimeOptions(args.toArray(new String[args.size()]));
        final cucumber.runtime.Runtime runtime = new Runtime(resourceLoader, classLoader, asList(mock(Backend.class)), runtimeOptions);
        runtime.run();
        return report;
    }

    private void assertXmlEqual(String expected, File actual) throws IOException, ParserConfigurationException, SAXException {
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = new Diff(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(expected)), new FileReader(actual));
        assertTrue("XML files are similar " + diff, diff.identical());
    }

}
