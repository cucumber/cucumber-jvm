package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.Formatter;
import gherkin.formatter.JSONFormatter;
import org.junit.Test;

import static cucumber.runtime.TestHelper.feature;
import static org.junit.Assert.assertEquals;

public class RuntimeTest {
    @Test
    public void runs_feature_with_json_formatter() throws Exception {
        CucumberFeature feature = feature("test.feature", "" +
                "Feature:\n" +
                "  Background:\n" +
                "    Given b\n" +
                "  Scenario:\n" +
                "    When s\n");
        StringBuilder out = new StringBuilder();
        JSONFormatter jsonFormatter = new JSONFormatter(out);
        new Runtime().run(feature, jsonFormatter, jsonFormatter);
        String expected = "{\"description\":\"\",\"name\":\"\",\"keyword\":\"Feature\",\"line\":1,\"elements\":[{\"description\":\"\",\"name\":\"\",\"keyword\":\"Background\",\"line\":2,\"steps\":[{\"result\":{\"status\":\"undefined\"},\"name\":\"b\",\"keyword\":\"Given \",\"line\":3,\"match\":{}}],\"type\":\"background\"},{\"description\":\"\",\"name\":\"\",\"keyword\":\"Scenario\",\"line\":4,\"steps\":[{\"result\":{\"status\":\"undefined\"},\"name\":\"s\",\"keyword\":\"When \",\"line\":5,\"match\":{}}],\"type\":\"scenario\"}]}";
        assertEquals(expected, out.toString());
    }
}
