package io.cucumber.compatibility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.order.StandardPickleOrders;
import io.cucumber.core.plugin.MessageFormatter;
import io.cucumber.core.runtime.Runtime;
import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.newOutputStream;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

public class CompatibilityTest {

    @ParameterizedTest
    @MethodSource("io.cucumber.compatibility.TestCase#testCases")
    void produces_expected_output_for(TestCase testCase) throws IOException {


        Path parentDir = Files.createDirectories(Paths.get("target", "messages", testCase.getId()));
        Path outputNdjson = parentDir.resolve("out.ndjson");

        try {
            PickleOrder pickleOrder = StandardPickleOrders.lexicalUriOrder();
            if ("multiple-features-reversed".equals(testCase.getId())) {
                pickleOrder = StandardPickleOrders.reverseLexicalUriOrder();
            }
            Runtime.builder()
                    .withRuntimeOptions(new RuntimeOptionsBuilder()
                            .addGlue(testCase.getGlue())
                            .setPickleOrder(pickleOrder)
                            .addFeature(testCase.getFeatures()).build())
                    .withAdditionalPlugins(
                            new MessageFormatter(newOutputStream(outputNdjson)))
                    .build()
                    .run();
        } catch (Exception e) {
            // exception: Scenario with unknown parameter types fails by
            // throwing an exceptions
            if (!"unknown-parameter-type".equals(testCase.getId())) {
                throw e;
            }
        }

        // exception: Cucumber JVM does not support named hooks
        if ("hooks-named".equals(testCase.getId())) {
            return;
        }

        // exception: Cucumber JVM does not support markdown features
        if ("markdown".equals(testCase.getId())) {
            return;
        }

        // exception: Cucumber JVM does not support retrying features
        if ("retry".equals(testCase.getId())) {
            return;
        }

        // exception: Cucumber JVM does not support messages for global hooks
        if ("global-hooks".equals(testCase.getId())
                || "global-hooks-afterall-error".equals(testCase.getId())
                || "global-hooks-attachments".equals(testCase.getId())
                || "global-hooks-beforeall-error".equals(testCase.getId())

        ) {
            return;
        }

        List<JsonNode> expected = readAllMessages(testCase.getExpectedFile());
        List<JsonNode> actual = readAllMessages(Files.newInputStream(outputNdjson));

        Map<String, List<JsonNode>> expectedEnvelopes = openEnvelopes(expected);
        Map<String, List<JsonNode>> actualEnvelopes = openEnvelopes(actual);

        // exception: Java step definitions and hooks are not in a predictable
        // order because Class#getMethods() does not return a predictable order.
        sortStepDefinitionsAndHooks(expectedEnvelopes);
        sortStepDefinitionsAndHooks(actualEnvelopes);

        // exception: Cucumber JVM needs a hook to access the scenario, remove
        // this hook from the actual test case.
        if ("attachments".equals(testCase.getId()) || "examples-tables-attachment".equals(testCase.getId())) {
            actualEnvelopes.getOrDefault("testCase", emptyList())
                    .forEach(jsonNode -> {
                        Iterator<JsonNode> testSteps = jsonNode.get("testSteps").iterator();
                        testSteps.next();
                        testSteps.remove();
                    });
        }

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
                        containsInRelativeOrder(aComparableMessage(messageType, expectedMessages)))));
    }

    private static List<JsonNode> readAllMessages(InputStream output) throws IOException {
        List<JsonNode> expectedEnvelopes = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        readAllLines(output).forEach(s -> {
            try {
                expectedEnvelopes.add(mapper.readTree(s));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        return expectedEnvelopes;
    }

    public static List<String> readAllLines(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, UTF_8))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }
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

    private void sortStepDefinitionsAndHooks(Map<String, List<JsonNode>> envelopes) {
        Comparator<JsonNode> stepDefinitionPatternComparator = Comparator
                .comparing(a -> a.get("pattern").get("source").asText());
        List<JsonNode> actualStepDefinitions = envelopes.get("stepDefinition");
        if (actualStepDefinitions != null) {
            actualStepDefinitions.sort(stepDefinitionPatternComparator);
        }
        Comparator<JsonNode> hookTypeComparator = Comparator.comparing(a -> a.get("type").asText());
        Comparator<JsonNode> hookTagExpressionComparator = Comparator.comparing(a -> {
            JsonNode tagExpression = a.get("tagExpression");
            if (tagExpression != null) {
                return tagExpression.asText();
            }
            return "";
        });
        List<JsonNode> actualHooks = envelopes.get("hook");
        if (actualHooks != null) {
            actualHooks.sort(hookTypeComparator.thenComparing(hookTagExpressionComparator));
        }
    }

    private static List<Matcher<? super JsonNode>> aComparableMessage(String messageType, List<JsonNode> messages) {
        return messages.stream()
                .map(jsonNode -> new AComparableMessage(messageType, jsonNode))
                .collect(Collectors.toList());
    }

}
