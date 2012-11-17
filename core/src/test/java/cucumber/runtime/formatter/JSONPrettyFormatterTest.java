package cucumber.runtime.formatter;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gherkin.formatter.JSONPrettyFormatter;
import gherkin.formatter.model.Step;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.cobertura.util.IOUtil;

import org.apache.tools.ant.util.FileUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.tools.shell.Global;
import org.xml.sax.SAXException;

import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.ClasspathResourceLoader;

import sun.misc.IOUtils;

public class JSONPrettyFormatterTest {

	@Test
	public void featureWithOutlineTest() throws Exception {
		File report = runFeaturesWithJSONPrettyFormatter(asList("cucumber/runtime/formatter/JSONPrettyFormatterTest.feature"));
		InputStreamReader control = new InputStreamReader(getClass().getResourceAsStream("JSONPrettyFormatterTest.json"), "UTF-8");

		String expected = FileUtils
				.readFully(new InputStreamReader(getClass().getResourceAsStream("JSONPrettyFormatterTest.json"), "UTF-8"));
		String actual = FileUtils.readFully(new InputStreamReader(new FileInputStream(report), "UTF-8"));
		assertEquals(expected, actual);
	}

	private File runFeaturesWithJSONPrettyFormatter(final List<String> featurePaths) throws IOException {
		File report = File.createTempFile("cucumber-jvm-junit", ".json");
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);

		List<String> args = new ArrayList<String>();
		args.add("--format");
		args.add("json-pretty:" + report.getAbsolutePath());
		args.addAll(featurePaths);

		RuntimeOptions runtimeOptions = new RuntimeOptions(new Properties(), args.toArray(new String[args.size()]));
		Backend backend = mock(Backend.class);
		when(backend.getSnippet(any(Step.class))).thenReturn("TEST SNIPPET");
		final cucumber.runtime.Runtime runtime = new Runtime(resourceLoader, classLoader, asList(backend), runtimeOptions);
		runtime.run();
		return report;
	}

}
