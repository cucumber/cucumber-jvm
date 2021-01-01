package io.cucumber.compatibility;

import io.cucumber.compatibility.matchers.AComparableMessage;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.plugin.HtmlFormatter;
import io.cucumber.core.plugin.JsonFormatter;
import io.cucumber.core.plugin.MessageFormatter;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.messages.Messages;
import io.cucumber.messages.NdjsonToMessageIterable;
import io.cucumber.messages.internal.com.google.protobuf.GeneratedMessageV3;
import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
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
        Path parentDir = Files.createDirectories(Paths.get("target", "messages", testCase.getId()));
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

        List<Messages.Envelope> expected = readAllMessages(testCase.getExpectedFile());
        List<Messages.Envelope> actual = readAllMessages(outputNdjson);

        Map<String, List<GeneratedMessageV3>> expectedEnvelopes = openEnvelopes(expected);
        Map<String, List<GeneratedMessageV3>> actualEnvelopes = openEnvelopes(actual);

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
            hasEntry(is(messageType), containsInRelativeOrder(aComparableMessage(expectedMessages)))));
    }

    private static List<Messages.Envelope> readAllMessages(Path output) throws IOException {
        List<Messages.Envelope> expectedEnvelopes = new ArrayList<>();
        InputStream input = Files.newInputStream(output);
        new NdjsonToMessageIterable(input)
                .forEach(expectedEnvelopes::add);
        return expectedEnvelopes;
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, List<T>> openEnvelopes(List<? extends GeneratedMessageV3> actual) {
        Map<String, List<T>> map = new LinkedHashMap<>();
        actual.forEach(envelope -> envelope.getAllFields()
                .forEach((fieldDescriptor, value) -> {
                    String jsonName = fieldDescriptor.getJsonName();
                    map.putIfAbsent(jsonName, new ArrayList<>());
                    map.get(jsonName).add((T) value);
                }));
        return map;
    }

    private void sortStepDefinitions(Map<String, List<GeneratedMessageV3>> envelopes) {
        Comparator<GeneratedMessageV3> stepDefinitionPatternComparator = (a, b) -> {
            Messages.StepDefinition sa = (Messages.StepDefinition) a;
            Messages.StepDefinition sb = (Messages.StepDefinition) b;
            return sa.getPattern().getSource().compareTo(sb.getPattern().getSource());
        };
        List<GeneratedMessageV3> actualStepDefinitions = envelopes.get("stepDefinition");
        if (actualStepDefinitions != null) {
            actualStepDefinitions.sort(stepDefinitionPatternComparator);
        }
    }

    private static List<Matcher<? super GeneratedMessageV3>> aComparableMessage(List<GeneratedMessageV3> messages) {
        return messages.stream()
                .map(AComparableMessage::new)
                .collect(Collectors.toList());
    }

}
