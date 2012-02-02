package cucumber.runtime.autocomplete;

import cucumber.io.Resource;
import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.ParameterType;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.model.CucumberFeature;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

public class StepdefGeneratorTest {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void generates_code_completion_metadata() throws IOException {
        StepdefGenerator meta = new StepdefGenerator();

        List<StepDefinition> stepDefs = asList(def("I have (\\d+) cukes in my belly"), def("I have (\\d+) apples in my bowl"));

        List<MetaStepdef> metadata = meta.generate(stepDefs, features());
        assertEquals("" +
                "[\n" +
                "  {\n" +
                "    \"source\": \"I have (\\\\d+) apples in my bowl\",\n" +
                "    \"flags\": \"\",\n" +
                "    \"steps\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"source\": \"I have (\\\\d+) cukes in my belly\",\n" +
                "    \"flags\": \"\",\n" +
                "    \"steps\": [\n" +
                "      {\n" +
                "        \"name\": \"I have 4 cukes in my belly\",\n" +
                "        \"args\": [\n" +
                "          {\n" +
                "            \"offset\": 7,\n" +
                "            \"val\": \"4\"\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"I have 42 cukes in my belly\",\n" +
                "        \"args\": [\n" +
                "          {\n" +
                "            \"offset\": 7,\n" +
                "            \"val\": \"42\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]",
                GSON.toJson(metadata));
    }

    private List<CucumberFeature> features() throws IOException {
        List<CucumberFeature> features = new ArrayList<CucumberFeature>();
        FeatureBuilder fb = new FeatureBuilder(features);
        fb.parse(new Resource() {
            @Override
            public String getPath() {
                return "test.feature";
            }

            @Override
            public InputStream getInputStream() {
                try {
                    return new ByteArrayInputStream(("" +
                            "Feature: Test\n" +
                            "  Scenario: Test\n" +
                            "    Given I have 4 cukes in my belly\n" +
                            "    And I have 3 bananas in my basket\n" +
                            "    Given I have 42 cukes in my belly\n")
                            .getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getClassName() {
                throw new UnsupportedOperationException();
            }
        }, emptyList());
        return features;
    }

    private StepDefinition def(final String pattern) {
        return new StepDefinition() {
            Pattern regexp = Pattern.compile(pattern);

            @Override
            public List<Argument> matchedArguments(Step step) {
                return new JdkPatternArgumentMatcher(regexp).argumentsFrom(step.getName());
            }

            @Override
            public String getLocation() {
                throw new UnsupportedOperationException("TODO");
            }

            @Override
            public List<ParameterType> getParameterTypes() {
                throw new UnsupportedOperationException("TODO");
            }

            @Override
            public void execute(Locale locale, Object[] args) throws Throwable {
                throw new UnsupportedOperationException("TODO");
            }

            @Override
            public boolean isDefinedAt(StackTraceElement stackTraceElement) {
                throw new UnsupportedOperationException("TODO");
            }

            @Override
            public String getPattern() {
                return pattern;
            }
        };
    }

}
