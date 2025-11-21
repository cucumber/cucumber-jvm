package io.cucumber.compatibility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.TextNode;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.isA;

public class CompatibilityTest {

    private static final Map<String, Map<Pattern, Matcher<?>>> exceptions = createExceptions();

    private static Map<String, Map<Pattern, Matcher<?>>> createExceptions() {
        Map<String, Map<Pattern, Matcher<?>>> exceptions = new LinkedHashMap<>();

        Map<Pattern, Matcher<?>> attachment = new LinkedHashMap<>();
        attachment.put(Pattern.compile("/testCaseStartedId"), isA(TextNode.class));
        attachment.put(Pattern.compile("/testStepId"), isA(TextNode.class));
        // exception: timestamps and durations are not predictable
        attachment.put(Pattern.compile("/timestamp/seconds"),  isA(NumericNode.class));
        attachment.put(Pattern.compile("/timestamp/nanos"),  isA(NumericNode.class));
        exceptions.put("attachment", attachment);

        Map<Pattern, Matcher<?>> meta = new LinkedHashMap<>();
        // exception: protocolVersion can vary
        meta.put(Pattern.compile("/protocolVersion"), isA(TextNode.class));
        // exception: Mata fields depend on the platform
        meta.put(Pattern.compile("/implementation/name"), isA(TextNode.class));
        meta.put(Pattern.compile("/implementation/version"), isA(TextNode.class));
        meta.put(Pattern.compile("/cpu/name"), isA(TextNode.class));
        meta.put(Pattern.compile("/os/name"), isA(TextNode.class));
        meta.put(Pattern.compile("/os/version"), isA(TextNode.class));
        meta.put(Pattern.compile("/runtime/name"), isA(TextNode.class));
        meta.put(Pattern.compile("/runtime/version"), isA(TextNode.class));
        // exceptioN: Ci information depends on where the tests are ran
        Matcher<JsonNode> value = anyOf(isA(MissingNode.class), isA(TextNode.class));
        meta.put(Pattern.compile("/ci/name"), value);
        meta.put(Pattern.compile("/ci/url"), value);
        meta.put(Pattern.compile("/ci/buildNumber"), value);
        meta.put(Pattern.compile("/ci/git/revision"), value);
        meta.put(Pattern.compile("/ci/git/remote"), value);
        meta.put(Pattern.compile("/ci/git/branch"), value);
        exceptions.put("meta", meta);

        Map<Pattern, Matcher<?>> source = new LinkedHashMap<>();
        source.put(Pattern.compile("/uri"), isA(TextNode.class));
        exceptions.put("source", source);

        Map<Pattern, Matcher<?>> gherkinDocument = new LinkedHashMap<>();
        // exception: ids are not predictable
        gherkinDocument.put(Pattern.compile("/feature/children/.*/scenario/id"), isA(TextNode.class));
        gherkinDocument.put(Pattern.compile("/feature/children/.*/scenario/steps/.*/id"), isA(TextNode.class));
        gherkinDocument.put(Pattern.compile("/feature/children/.*/scenario/examples/.*/id"), isA(TextNode.class));
        gherkinDocument.put(Pattern.compile("/feature/children/.*/rule/id"), isA(TextNode.class));
        gherkinDocument.put(Pattern.compile("/feature/children/.*/rule/tags/.*/id"), isA(TextNode.class));
        gherkinDocument.put(Pattern.compile("/feature/children/.*/scenario/tags/.*/id"), isA(TextNode.class));
        gherkinDocument.put(Pattern.compile("/feature/children/.*/background/id"),  isA(TextNode.class));
        gherkinDocument.put(Pattern.compile("/feature/children/.*/background/steps/.*/id"),  isA(TextNode.class));
        // exception: the CCK uses relative paths as uris
        gherkinDocument.put(Pattern.compile("/uri"), isA(TextNode.class));
        exceptions.put("gherkinDocument", gherkinDocument);

        Map<Pattern, Matcher<?>> pickle = new LinkedHashMap<>();
        // exception: ids are not predictable
        pickle.put(Pattern.compile("/id"), isA(TextNode.class));
        pickle.put(Pattern.compile("/uri"), isA(TextNode.class));
        pickle.put(Pattern.compile("/astNodeIds/.*"), isA(TextNode.class));
        pickle.put(Pattern.compile("/steps/.*/id"), isA(TextNode.class));
        pickle.put(Pattern.compile("/steps/.*/astNodeIds/.*"), isA(TextNode.class));
        pickle.put(Pattern.compile("/tags/.*/astNodeId"), isA(TextNode.class));
        pickle.put(Pattern.compile("/name"), isA(TextNode.class));
        exceptions.put("pickle", pickle);

        Map<Pattern, Matcher<?>> stepDefinition = new LinkedHashMap<>();
        // exception: ids are not predictable
        stepDefinition.put(Pattern.compile("/id"), isA(TextNode.class));
        // exception: the CCK uses relative paths as uris
        stepDefinition.put(Pattern.compile("/sourceReference/uri"), isA(MissingNode.class));
        stepDefinition.put(Pattern.compile("/sourceReference/location/line"), isA(MissingNode.class));
        exceptions.put("stepDefinition", stepDefinition);

        Map<Pattern, Matcher<?>> testRunStarted = new LinkedHashMap<>();
        // exception: not yet implemented
        testRunStarted.put(Pattern.compile("/id"), isA(MissingNode.class));
        // exception: timestamps and durations are not predictable
        testRunStarted.put(Pattern.compile("/timestamp/seconds"), isA(NumericNode.class));
        testRunStarted.put(Pattern.compile("/timestamp/nanos"), isA(NumericNode.class));
        exceptions.put("testRunStarted", testRunStarted);

        Map<Pattern, Matcher<?>> testCase = new LinkedHashMap<>();
        // exception: ids are not predictable
        testCase.put(Pattern.compile("/id"), isA(TextNode.class));
        testCase.put(Pattern.compile("/pickleId"), isA(TextNode.class));
        testCase.put(Pattern.compile("/testSteps/.*/id"), isA(TextNode.class));
        testCase.put(Pattern.compile("/testSteps/.*/pickleStepId"), isA(TextNode.class));
        testCase.put(Pattern.compile("/testSteps/.*/stepDefinitionIds/.*"), isA(TextNode.class));
        testCase.put(Pattern.compile("/testSteps/.*/hookId"), isA(TextNode.class));
        // exception: not yet implemented
        testCase.put(Pattern.compile("/testRunStartedId"), isA(MissingNode.class));
        exceptions.put("testCase", testCase);

        Map<Pattern, Matcher<?>> testCaseStarted = new LinkedHashMap<>();
        // exception: ids are not predictable
        testCaseStarted.put(Pattern.compile("/id"), isA(TextNode.class));
        testCaseStarted.put(Pattern.compile("/testCaseId"), isA(TextNode.class));
        // exception: timestamps and durations are not predictable
        testCaseStarted.put(Pattern.compile("/timestamp/seconds"), isA(IntNode.class));
        testCaseStarted.put(Pattern.compile("/timestamp/nanos"), isA(IntNode.class));
        exceptions.put("testCaseStarted", testCaseStarted);

        Map<Pattern, Matcher<?>> testStepStarted = new LinkedHashMap<>();
        testStepStarted.put(Pattern.compile("/testCaseStartedId"), isA(TextNode.class));
        testStepStarted.put(Pattern.compile("/testStepId"), isA(TextNode.class));
        // exception: timestamps and durations are not predictable
        testStepStarted.put(Pattern.compile("/timestamp/seconds"), isA(IntNode.class));
        testStepStarted.put(Pattern.compile("/timestamp/nanos"), isA(IntNode.class));
        exceptions.put("testStepStarted", testStepStarted);

        Map<Pattern, Matcher<?>> testStepFinished = new LinkedHashMap<>();
        // exception: ids are not predictable
        testStepFinished.put(Pattern.compile("/testCaseStartedId"), isA(TextNode.class));
        // exception: ids are not predictable
        testStepFinished.put(Pattern.compile("/testStepId"), isA(TextNode.class));
        // exception: timestamps and durations are not predictable
        testStepFinished.put(Pattern.compile("/testStepResult/duration/seconds"), isA(IntNode.class));
        testStepFinished.put(Pattern.compile("/testStepResult/duration/nanos"), isA(IntNode.class));
        // exception: error messages are platform specific
        testStepFinished.put(Pattern.compile("/testStepResult/message"), isA(TextNode.class));
        // exception: exceptions are platform specific
        testStepFinished.put(Pattern.compile("/testStepResult/exception/type"), isA(TextNode.class));
        testStepFinished.put(Pattern.compile("/testStepResult/exception/message"), isA(TextNode.class));
        testStepFinished.put(Pattern.compile("/testStepResult/exception/stackTrace"), isA(TextNode.class));
        // exception: timestamps and durations are not predictable
        testStepFinished.put(Pattern.compile("/timestamp/seconds"), isA(IntNode.class));
        testStepFinished.put(Pattern.compile("/timestamp/nanos"), isA(IntNode.class));
        exceptions.put("testStepFinished", testStepFinished);

        Map<Pattern, Matcher<?>> testCaseFinished = new LinkedHashMap<>();
        // exception: ids are not predictable
        testCaseFinished.put(Pattern.compile("/testCaseStartedId"), isA(TextNode.class));
        // exception: timestamps and durations are not predictable
        testCaseFinished.put(Pattern.compile("/timestamp/seconds"), isA(IntNode.class));
        testCaseFinished.put(Pattern.compile("/timestamp/nanos"), isA(IntNode.class));
        exceptions.put("testCaseFinished", testCaseFinished);

        Map<Pattern, Matcher<?>> testRunFinished = new LinkedHashMap<>();
        // exception: not yet implemented
        testRunFinished.put(Pattern.compile("/testRunStartedId"), isA(MissingNode.class));
        // exception: timestamps and durations are not predictable
        testRunFinished.put(Pattern.compile("/timestamp/seconds"), isA(IntNode.class));
        testRunFinished.put(Pattern.compile("/timestamp/nanos"), isA(IntNode.class));
        exceptions.put("testRunFinished", testRunFinished);

        Map<Pattern, Matcher<?>> hook = new LinkedHashMap<>();
        // exception: ids are not predictable
        hook.put(Pattern.compile("/id"), isA(TextNode.class));
        // exception: the CCK expects source references with URIs but
        // Java can only provide method and stack trace references.
        hook.put(Pattern.compile("/sourceReference/uri"), isA(MissingNode.class));
        hook.put(Pattern.compile("/sourceReference/location/line"), isA(MissingNode.class));
        exceptions.put("hook", hook);

        Map<Pattern, Matcher<?>> parameterType = new LinkedHashMap<>();
        // exception: ids are not predictable
        parameterType.put(Pattern.compile("/id"), isA(TextNode.class));
        // exception: the CCK uses relative paths as uris
        parameterType.put(Pattern.compile("/sourceReference/uri"), isA(MissingNode.class));
        parameterType.put(Pattern.compile("/sourceReference/location/line"), isA(MissingNode.class));
        exceptions.put("parameterType", parameterType);

        Map<Pattern, Matcher<?>> suggestion = new LinkedHashMap<>();
        // exception: ids are not predictable
        suggestion.put(Pattern.compile("/id"),  isA(TextNode.class));
        suggestion.put(Pattern.compile("/pickleStepId"),  isA(TextNode.class));
        // exception: language is implementation specific
        suggestion.put(Pattern.compile("/snippets/.*/language"),  isA(TextNode.class));
        // exception: code is implementation specific
        suggestion.put(Pattern.compile("/snippets/.*/code"),  isA(TextNode.class));

        exceptions.put("suggestion", suggestion);

        return exceptions;
    }

    @ParameterizedTest
    @MethodSource("io.cucumber.compatibility.TestCase#testCases")
    void produces_expected_output_for(TestCase testCase) throws IOException {

        Path parentDir = Files.createDirectories(Paths.get("target", "messages", testCase.getId()));
        Path actualNdjson = parentDir.resolve("actual.ndjson");
        Path expectedNdjson = parentDir.resolve("expected.ndjson");
        Files.copy(testCase.getExpectedFile(), expectedNdjson, REPLACE_EXISTING);

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
                        new MessageFormatter(newOutputStream(actualNdjson)))
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
        if ("retry".equals(testCase.getId())
            || "retry-ambiguous".equals(testCase.getId())
            || "retry-pending".equals(testCase.getId())
        ) {
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
        List<JsonNode> actual = readAllMessages(Files.newInputStream(actualNdjson));

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
            expectedEnvelopes.remove("suggestion");
        }
         
        if ("undefined".equals(testCase.getId())) {
            // bug: Cucumber JVM doesn't produce a suggestion that matches float
            ((ArrayNode) expectedEnvelopes.get("suggestion").get(3).get("snippets")).remove(1);
        }
        if ("ambiguous".equals(testCase.getId())) {
            // bug: Cucumber JVM doesn't include the ambiguous step definitions
            // https://github.com/cucumber/cucumber-jvm/issues/3006
            expectedEnvelopes.remove("testCase");
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
                .map(jsonNode -> new AComparableMessage(messageType, jsonNode,
                    exceptions.getOrDefault(messageType, emptyMap())))
                .collect(Collectors.toList());
    }

}
