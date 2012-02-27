package cucumber.formatter;

import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class UnitFormatterTest {

    @Test
    public void featureSimpleTest() throws Exception {
        runFeaturesWithFormatter(asList("cucumber/formatter/UnitFormatterTest_1.feature"));
        compareXML("cucumber/formatter/UnitFormatterTest_1.report.xml", "report.xml");
    }

    @Test
    public void featureWithBackgroundTest() throws Exception {
        runFeaturesWithFormatter(asList("cucumber/formatter/UnitFormatterTest_2.feature"));
        compareXML("cucumber/formatter/UnitFormatterTest_2.report.xml", "report.xml");
    }

//    @Test
//    public void featureWithOutlineTest() throws Exception {
//        runFeaturesWithFormatter(asList("cucumber/formatter/UnitFormatterTest_3.feature"));
//        compareXML("cucumber/formatter/UnitFormatterTest_3.report.xml", "report.xml");
//    }

    private void runFeaturesWithFormatter(final List<String> featurePaths) throws IOException {
        File report = new File("report.xml");
//        report.deleteOnExit();
        final UnitFormatter f = new UnitFormatter(report);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        final List<String> gluePaths = emptyList();
        final cucumber.runtime.Runtime runtime = new Runtime(resourceLoader, gluePaths, classLoader, asList(mock(Backend.class)), false);
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
