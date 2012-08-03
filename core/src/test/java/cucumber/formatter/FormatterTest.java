package cucumber.formatter;

import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertTrue;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.mockito.Mockito.mock;

public class FormatterTest {

    @Test
    public void junitFeatureSimpleTest() throws Exception {
        File report = runFeaturesWithJunitFormatter(asList("cucumber/formatter/FormatterTest_1.feature"));
        assertXmlEqual("cucumber/formatter/JUnitFormatterTest_1.report.xml", report);
    }

    @Test
    public void junitFeatureWithBackgroundTest() throws Exception {
        File report = runFeaturesWithJunitFormatter(asList("cucumber/formatter/FormatterTest_2.feature"));
        assertXmlEqual("cucumber/formatter/JUnitFormatterTest_2.report.xml", report);
    }

    @Test
    public void junitFeatureWithOutlineTest() throws Exception {
        File report = runFeaturesWithJunitFormatter(asList("cucumber/formatter/FormatterTest_3.feature"));
        assertXmlEqual("cucumber/formatter/JUnitFormatterTest_3.report.xml", report);
    }
    
    @Test
    public void jsonFeatureSimpleTest() throws Exception {
        File report = runFeaturesWithJsonFormatter(asList("cucumber/formatter/FormatterTest_1.feature"));
        assertJsonEqual("cucumber/formatter/JsonFormatterTest_1.report.js", report);
    }

    @Test
    public void jsonFeatureWithBackgroundTest() throws Exception {
        File report = runFeaturesWithJsonFormatter(asList("cucumber/formatter/FormatterTest_2.feature"));
        assertJsonEqual("cucumber/formatter/JsonFormatterTest_2.report.js", report);
    }

    @Test
    public void jsonFeatureWithOutlineTest() throws Exception {
        File report = runFeaturesWithJsonFormatter(asList("cucumber/formatter/FormatterTest_3.feature"));
        assertJsonEqual("cucumber/formatter/JsonFormatterTest_3.report.js", report);
    }
    

    private File runFeaturesWithJsonFormatter(final List<String> featurePaths) throws IOException {
        return runFeaturesWithFormatter(featurePaths, "json");
    }
    
    private File runFeaturesWithJunitFormatter(final List<String> featurePaths) throws IOException {
        return runFeaturesWithFormatter(featurePaths, "junit");
    }

    private File runFeaturesWithFormatter(final List<String> featurePaths, String formatter) throws IOException {
        File report = File.createTempFile("cucumber-jvm-junit", "xml");
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);

        List<String> args = new ArrayList<String>();
        args.add("--format");
        args.add(formatter+":" + report.getAbsolutePath());
        args.addAll(featurePaths);

        RuntimeOptions runtimeOptions = new RuntimeOptions(new Properties(), args.toArray(new String[args.size()]));
        final cucumber.runtime.Runtime runtime = new Runtime(resourceLoader, classLoader, asList(mock(Backend.class)), runtimeOptions);
        runtime.run();
        return report;
    }

    private String resourceToString(String resourceLocation) throws IOException {
        Reader reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceLocation), "UTF-8");
        String output = IOUtils.toString(reader);
        reader.close();
        return output;
    }

    
    private void assertJsonEqual(String expectedFile, File actualFile) throws IOException {
    	Reader reader = new FileReader(actualFile);
        String actual = IOUtils.toString(reader);
        reader.close();
        System.out.println(actual);
        String expected = resourceToString(expectedFile);
        assertJsonEquals(expected,actual);
    }
    
    private void assertXmlEqual(String expected, File actual) throws IOException, ParserConfigurationException, SAXException {
        XMLUnit.setIgnoreWhitespace(true);
        InputStreamReader control = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(expected), "UTF-8");
        Diff diff = new Diff(control, new FileReader(actual));
        assertTrue("XML files are similar " + diff, diff.identical());
    }

}
