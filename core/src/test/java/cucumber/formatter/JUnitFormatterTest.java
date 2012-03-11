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
import java.io.*;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Uladzimir Mihura
 *         Date: 2/25/12
 *         Time: 8:39 PM
 */
public class JUnitFormatterTest {

    @Test
    public void featureSimpleTest() throws Exception {
        runFeaturesWithFormatter(asList("cucumber/formatter/JUnitFormatterTest_1.feature"));
        compareXML("cucumber/formatter/JUnitFormatterTest_1.report.xml", "report.xml");
    }

    @Test
    public void featureWithBackgroundTest() throws Exception {
        runFeaturesWithFormatter(asList("cucumber/formatter/JUnitFormatterTest_2.feature"));
        compareXML("cucumber/formatter/JUnitFormatterTest_2.report.xml", "report.xml");
    }

    @Test
    public void featureWithOutlineTest() throws Exception {
        runFeaturesWithFormatter(asList("cucumber/formatter/JUnitFormatterTest_3.feature"));
        compareXML("cucumber/formatter/JUnitFormatterTest_3.report.xml", "report.xml");
    }

    private void runFeaturesWithFormatter(final List<String> featurePaths) throws IOException {
        File report = new File("report.xml");
//        report.deleteOnExit();
        final JUnitFormatter f = new JUnitFormatter(report);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        final List<String> gluePaths = emptyList();
        RuntimeOptions runtimeOptions = new RuntimeOptions();
        final cucumber.runtime.Runtime runtime = new Runtime(resourceLoader, classLoader, asList(mock(Backend.class)), runtimeOptions);
        runtime.run(featurePaths, emptyList(), f, f);
        f.done();
        f.close();
    }

    private void compareXML(String expected, String received) throws IOException, ParserConfigurationException, SAXException {
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = new Diff(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(expected)), new FileReader(received));
        assertTrue("XML files are similar " + diff, diff.identical());
    }

}
