package cucumber.runtime.autocomplete;

import cucumber.runtime.ExpressionArgumentMatcher;
import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.io.Resource;
import cucumber.runtime.model.CucumberFeature;
import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.pickles.PickleStep;
import io.cucumber.cucumberexpressions.Argument;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.TransformLookup;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class StepdefGeneratorTest {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void generates_code_completion_metadata() throws IOException {
        StepdefGenerator meta = new StepdefGenerator();

        List<StepDefinition> stepDefs = asList(def("I have {arg1} cukes in my belly"), def("I have {arg1} apples in my bowl"));

        List<MetaStepdef> metadata = meta.generate(stepDefs, features());
        String expectedJson = "" +
                "[\n" +
                "  {\n" +
                "    \"source\": \"I have {arg1} apples in my bowl\",\n" +
                "    \"flags\": \"\",\n" +
                "    \"steps\": []\n" +
                "  },\n" +
                "  {\n" +
                "    \"source\": \"I have {arg1} cukes in my belly\",\n" +
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
                "]";
        assertEquals(GSON.fromJson(expectedJson, new TypeReference<List<MetaStepdef>>() {
        }.getType()), metadata);
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
            public String getAbsolutePath() {
                throw new UnsupportedOperationException();
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
            public String getClassName(String extension) {
                throw new UnsupportedOperationException();
            }
        });
        return features;
    }

    private StepDefinition def(final String pattern) {
        return new StepDefinition() {
            ExpressionFactory factory = new ExpressionFactory(new TransformLookup(Locale.ENGLISH));
            Expression expression = factory.createExpression(pattern, asList((Type)Integer.class));

            @Override
            public List<Argument> matchedArguments(PickleStep step) {
                return new ExpressionArgumentMatcher(expression).argumentsFrom(step.getText());
            }

            @Override
            public String getLocation(boolean detail) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Integer getParameterCount() {
                return null;
            }

            @Override
            public ParameterInfo getParameterType(int n, Type argumentType) {
                return null;
            }

            @Override
            public void execute(String language, Object[] args) throws Throwable {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isDefinedAt(StackTraceElement stackTraceElement) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getPattern() {
                return pattern;
            }

            @Override
            public boolean isScenarioScoped() {
                return false;
            }
        };
    }

    public abstract static class TypeReference<T> {
        private final Type type;

        protected TypeReference() {
            Type superclass = getClass().getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        }

        public Type getType() {
            return this.type;
        }
    }
}
