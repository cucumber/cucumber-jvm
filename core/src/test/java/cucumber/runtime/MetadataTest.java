package cucumber.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cucumber.resources.AbstractResource;
import cucumber.resources.PathWithLines;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

public class MetadataTest {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void generates_code_completion_metadata() {
        Metadata meta = new Metadata();

        List<StepDefinition> stepDefs = asList(def("I have (\\d+) cukes in my belly"), def("I have (\\d+) apples in my bowl"));

        Map<String, List<String>> metadata = meta.generate(stepDefs, features());
        assertEquals("" +
                "{\n" +
                "  \"I have (\\\\d+) cukes in my belly\": [\n" +
                "    \"I have 4 cukes in my belly\",\n" +
                "    \"I have 42 cukes in my belly\"\n" +
                "  ],\n" +
                "  \"I have (\\\\d+) apples in my bowl\": []\n" +
                "}",
                GSON.toJson(metadata));
    }

    private List<CucumberFeature> features() {
        List<CucumberFeature> features = new ArrayList<CucumberFeature>();
        FeatureBuilder fb = new FeatureBuilder(features);
        fb.parse(new AbstractResource(new PathWithLines("test.feature")) {
            @Override
            public String getPath() {
                return pathWithLines.path;
            }

            @Override
            public InputStream getInputStream()  {
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
            public Class getTypeForTableList(int argIndex) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getLocation() {
                throw new UnsupportedOperationException("TODO");
            }

            @Override
            public Class<?>[] getParameterTypes() {
                throw new UnsupportedOperationException("TODO");
            }

            @Override
            public void execute(Object[] args) throws Throwable {
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
