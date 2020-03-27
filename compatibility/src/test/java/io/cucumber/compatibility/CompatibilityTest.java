package io.cucumber.compatibility;

import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.plugin.MessageFormatter;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.Messages;
import io.cucumber.messages.NdjsonToMessageIterable;
import io.cucumber.messages.internal.com.google.protobuf.GeneratedMessageV3;
import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

public class CompatibilityTest {

    @ParameterizedTest
    @MethodSource("io.cucumber.compatibility.TestCase#testCases")
    void produces_expected_output_for(TestCase testCase) throws IOException {
        File parentDir = new File("target/messages/" + testCase.getId());
        parentDir.mkdirs();
        File output = new File(parentDir, "out.ndjson");

        try {
            Runtime.builder()
                .withRuntimeOptions(new RuntimeOptionsBuilder()
                    .addGlue(testCase.getGlue())
                    .addFeature(testCase.getFeature())
                    .build())
                .withAdditionalPlugins(new MessageFormatter(new FileOutputStream(output)))
                .withEventBus(new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID))
                .build()
                .run();
        } catch (Exception ignored) {

        }

        List<Messages.Envelope> expected = readAllMessages(testCase.getExpectedFile());
        List<Messages.Envelope> actual = readAllMessages(output.toPath());

        Map<String, List<GeneratedMessageV3>> expectedEnvelopes = openEnvelopes(expected);
        Map<String, List<GeneratedMessageV3>> actualEnvelopes = openEnvelopes(actual);

        expectedEnvelopes.forEach((messageType, expectedMessages) ->
            assertThat(
                actualEnvelopes,
                hasEntry(is(messageType), containsInRelativeOrder(aComparableMessage(expectedMessages)))
            )
        );
    }

    private static List<Messages.Envelope> readAllMessages(Path output) throws IOException {
        List<Messages.Envelope> expectedEnvelopes = new ArrayList<>();
        InputStream input = Files.newInputStream(output);
        new NdjsonToMessageIterable(input)
            .forEach(expectedEnvelopes::add);
        return expectedEnvelopes;
    }

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


    private static List<Matcher<? super GeneratedMessageV3>> aComparableMessage(List<GeneratedMessageV3> expectedMessages) {
        return expectedMessages.stream()
            .map(AComparableMessage::new)
            .collect(Collectors.toList());
    }
}


