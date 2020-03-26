package io.cucumber.compatibility;

import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.plugin.MessageFormatter;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.Messages;
import io.cucumber.messages.NdjsonToMessageIterable;
import io.cucumber.messages.internal.com.google.protobuf.GeneratedMessageV3;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.time.Clock.fixed;
import static java.time.Instant.ofEpochSecond;
import static java.time.ZoneOffset.UTC;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

public class CompatibilityTest {

    private final AtomicLong id = new AtomicLong();
    private final Supplier<UUID> idGenerator = () -> new UUID(0L, id.getAndIncrement());

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
                .withEventBus(new TimeServiceEventBus(fixed(ofEpochSecond(0), UTC), idGenerator))
                .build()
                .run();
        } catch (Exception ignored) {

        }

        List<Messages.Envelope> expected = readAllMessages(testCase.getExpectedFile());
        List<Messages.Envelope> actual = readAllMessages(output.toPath());

        Map<String, List<GeneratedMessageV3>> expectedEnvelopes = openEnvelope(expected);
        Map<String, List<GeneratedMessageV3>> actualEnvelopes = openEnvelope(actual);

        expectedEnvelopes.forEach((messageType, expectedMessages) ->
            assertThat(
                actualEnvelopes,
                hasEntry(is(messageType), containsInAnyOrder(aComparableMessage(expectedMessages)))
            )
        );
    }

    private static Collection<Matcher<?>> aComparableMessage(List<?> expectedMessages) {
        return expectedMessages.stream()
            .map(element -> {
                if (element instanceof GeneratedMessageV3) {
                    GeneratedMessageV3 message = (GeneratedMessageV3) element;
                    return new GeneratedMessageV3TypeSafeDiagnosingMatcher(message);
                }
                return CoreMatchers.is(element);

            })
            .collect(Collectors.toList());
    }

    private static <T> Map<String, List<T>> openEnvelope(List<? extends GeneratedMessageV3> actual) {
        Map<String, List<T>> map = new LinkedHashMap<>();
        actual.forEach(envelope -> envelope.getAllFields()
            .forEach((fieldDescriptor, value) -> {
                String jsonName = fieldDescriptor.getJsonName();
                map.putIfAbsent(jsonName, new ArrayList<>());
                map.get(jsonName).add((T) value);
            }));
        return map;
    }

    private static List<Messages.Envelope> readAllMessages(Path output) throws IOException {
        List<Messages.Envelope> expectedEnvelopes = new ArrayList<>();
        InputStream input = Files.newInputStream(output);
        new NdjsonToMessageIterable(input)
            .forEach(expectedEnvelopes::add);
        return expectedEnvelopes;
    }

    private static class GeneratedMessageV3TypeSafeDiagnosingMatcher extends TypeSafeDiagnosingMatcher<GeneratedMessageV3> {

        private final List<Matcher<?>> expected = new ArrayList<>();

        public GeneratedMessageV3TypeSafeDiagnosingMatcher(GeneratedMessageV3 expected) {
            openEnvelope(Collections.singletonList(expected))
                .forEach((messageType, expectedMessages) ->
                    this.expected.add(hasEntry(is(messageType), containsInAnyOrder(aComparableMessage(expectedMessages)))));
        }

        @Override
        public void describeTo(Description description) {
            description.appendList("", ",\n", "", expected);
        }

        @Override
        protected boolean matchesSafely(GeneratedMessageV3 actual, Description mismatchDescription) {
            return expected.equals(actual);
        }
    }
}
