package io.cucumber.compatibility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.plugin.HtmlFormatter;
import io.cucumber.core.plugin.JsonFormatter;
import io.cucumber.core.plugin.MessageFormatter;
import io.cucumber.core.runtime.Runtime;
import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.file.Files.newOutputStream;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

public class CompatibilityTest {

    @ParameterizedTest
    @MethodSource("io.cucumber.compatibility.TestCase#testCases")
    void produces_expected_output_for(TestCase testCase) throws IOException {
        Path parentDir = Files.createDirectories(Paths.get("target", "messages",
            testCase.getId()));
        Path outputNdjson = parentDir.resolve("out.ndjson");
        Path outputHtml = parentDir.resolve("out.html");
        Path outputJson = parentDir.resolve("out.json");

        try {
            Runtime.builder()
                    .withRuntimeOptions(new RuntimeOptionsBuilder()
                            .addGlue(testCase.getGlue())
                            .addFeature(testCase.getFeature())
                            .build())
                    .withAdditionalPlugins(
                        new MessageFormatter(newOutputStream(outputNdjson)),
                        new HtmlFormatter(newOutputStream(outputHtml)),
                        new JsonFormatter(newOutputStream(outputJson)))
                    .build()
                    .run();
        } catch (Exception ignored) {

        }

        List<JsonNode> expected = readAllMessages(testCase.getExpectedFile());
        List<JsonNode> actual = readAllMessages(outputNdjson);

        Map<String, List<JsonNode>> expectedEnvelopes = openEnvelopes(expected);
        Map<String, List<JsonNode>> actualEnvelopes = openEnvelopes(actual);

        // exception: Java step definitions are not in a predictable order
        // because Class#getMethods() does not return a predictable order.
        sortStepDefinitions(expectedEnvelopes);
        sortStepDefinitions(actualEnvelopes);

        // exception: Cucumber JVM can't execute when there are
        // unknown-parameter-types
        if ("unknown-parameter-type".equals(testCase.getId())) {
            expectedEnvelopes.remove("testCase");
            expectedEnvelopes.remove("testCaseStarted");
            expectedEnvelopes.remove("testStepStarted");
            expectedEnvelopes.remove("testStepFinished");
            expectedEnvelopes.remove("testCaseFinished");
        }

        expectedEnvelopes.forEach((messageType, expectedMessages) -> assertThat(
            actualEnvelopes,
            hasEntry(is(messageType),
                containsInRelativeOrder(aComparableMessage(expectedMessages)))));
    }

    private static List<JsonNode> readAllMessages(Path output) throws IOException {
        List<JsonNode> expectedEnvelopes = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Files.readAllLines(output).forEach(s -> {
            try {
                expectedEnvelopes.add(mapper.readTree(s));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        return expectedEnvelopes;
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, List<T>> openEnvelopes(List<JsonNode> actual) {
        Map<String, List<T>> map = new LinkedHashMap<>();
        actual.forEach(envelope -> envelope.fieldNames()
                .forEachRemaining(fieldName -> {
                    map.putIfAbsent(fieldName, new ArrayList<>());
                    map.get(fieldName).add((T) envelope.get(fieldName));
                }));
        return map;
    }

    private void sortStepDefinitions(Map<String, List<JsonNode>> envelopes) {
        Comparator<JsonNode> stepDefinitionPatternComparator = Comparator
                .comparing(a -> a.get("pattern").get("source").asText());
        List<JsonNode> actualStepDefinitions = envelopes.get("stepDefinition");
        if (actualStepDefinitions != null) {
            actualStepDefinitions.sort(stepDefinitionPatternComparator);
        }
    }

    private static List<Matcher<? super JsonNode>> aComparableMessage(List<JsonNode> messages) {
        return messages.stream()
                .map(AComparableMessage::new)
                .collect(Collectors.toList());
    }

}
