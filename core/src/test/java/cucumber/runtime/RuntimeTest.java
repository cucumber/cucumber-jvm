package cucumber.runtime;

import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.JSONFormatter;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static cucumber.runtime.TestHelper.feature;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class RuntimeTest {
    @Test
    public void runs_feature_with_json_formatter() throws Exception {
        CucumberFeature feature = feature("test.feature", "" +
                "Feature: feature name\n" +
                "  Background: background name\n" +
                "    Given b\n" +
                "  Scenario: scenario name\n" +
                "    When s\n");
        StringBuilder out = new StringBuilder();
        JSONFormatter jsonFormatter = new JSONFormatter(out);
        List<Backend> backends = asList(mock(Backend.class));
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        new Runtime(new ClasspathResourceLoader(classLoader), Collections.<String>emptyList(), classLoader, backends, true).run(feature, jsonFormatter, jsonFormatter);
        jsonFormatter.done();
        String expected = "[{\"id\":\"feature-name\",\"description\":\"\",\"name\":\"feature name\",\"keyword\":\"Feature\",\"line\":1,\"elements\":[{\"description\":\"\",\"name\":\"background name\",\"keyword\":\"Background\",\"line\":2,\"steps\":[{\"result\":{\"status\":\"undefined\"},\"name\":\"b\",\"keyword\":\"Given \",\"line\":3,\"match\":{}}],\"type\":\"background\"},{\"id\":\"feature-name;scenario-name\",\"description\":\"\",\"name\":\"scenario name\",\"keyword\":\"Scenario\",\"line\":4,\"steps\":[{\"result\":{\"status\":\"undefined\"},\"name\":\"s\",\"keyword\":\"When \",\"line\":5,\"match\":{}}],\"type\":\"scenario\"}],\"uri\":\"test.feature\"}]";
        assertEquals(expected, out.toString());
    }
}
