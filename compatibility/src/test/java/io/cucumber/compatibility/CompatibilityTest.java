package io.cucumber.compatibility;

import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.plugin.MessageFormatter;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.messages.Messages;
import io.cucumber.messages.NdjsonToMessageIterable;
import io.cucumber.messages.internal.com.google.protobuf.ByteString;
import io.cucumber.messages.internal.com.google.protobuf.Descriptors.EnumValueDescriptor;
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
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
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

        Map<String, List<GeneratedMessageV3>> expectedEnvelopes = openEnvelope(expected);
        Map<String, List<GeneratedMessageV3>> actualEnvelopes = openEnvelope(actual);

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


    private static List<Matcher<? super GeneratedMessageV3>> aComparableMessage(List<GeneratedMessageV3> expectedMessages) {
        return expectedMessages.stream()
            .map(GeneratedMessageV3TypeSafeDiagnosingMatcher::new)
            .collect(Collectors.toList());
    }

    private static class GeneratedMessageV3TypeSafeDiagnosingMatcher extends TypeSafeDiagnosingMatcher<GeneratedMessageV3> {

        private final List<Matcher<?>> expected = new ArrayList<>();
        private final GeneratedMessageV3 expectedMessage;
        private final int depth;


        public GeneratedMessageV3TypeSafeDiagnosingMatcher(GeneratedMessageV3 expectedMessage) {
            this(expectedMessage, 0);
        }

        public GeneratedMessageV3TypeSafeDiagnosingMatcher(GeneratedMessageV3 expectedMessage, int depth) {
            this.expectedMessage = expectedMessage;
            this.depth = depth + 1;
            openEnvelope(singletonList(expectedMessage))
                .forEach((fieldName, expectedMessages) -> {
                    switch (fieldName) {
                        case "uri":
                            this.expected.add(hasEntry(is(fieldName), containsInRelativeOrder(aUriEndingWith(expectedMessages))));
                            break;
                        case "id":
                        case "pickleId":
                        case "astNodeId":
                        case "hookId":
                        case "pickleStepId":
                        case "testCaseId":
                        case "testStepId":
                        case "testCaseStartedId":
                            this.expected.add(hasEntry(is(fieldName), containsInRelativeOrder(anId(expectedMessages))));
                            break;
                        case "astNodeIds":
                        case "stepDefinitionIds":
                            this.expected.add(hasEntry(is(fieldName), containsInRelativeOrder(containsInRelativeOrder(anId(expectedMessages)))));
                            break;
                        case "sourceReference":
                            // TODO: Uris don't compare. We should use something else.
                            break;
                        case "timestamp":
                            this.expected.add(hasEntry(is(fieldName), containsInRelativeOrder(aTimeStamp(expectedMessages))));
                            break;
                        case "duration":
                            this.expected.add(hasEntry(is(fieldName), containsInRelativeOrder(aDuration(expectedMessages))));
                            break;
                        case "message":
                            // TODO: Errors don't usually match. But is this key only used for errors?
                            break;
                        default:
                            this.expected.add(hasEntry(is(fieldName), containsInRelativeOrder(aComparableElement(expectedMessages, this.depth))));
                    }
                });

        }

        @Override
        public void describeTo(Description description) {
            StringBuilder padding = new StringBuilder();
            for (int i = 0; i < depth + 1; i++) {
                padding.append("\t");
            }

            if (expected.isEmpty()) {
                description.appendText("\n" + padding.toString() + "all fields are ignored");
                return;
            }

            description.appendList("\n" + padding.toString(), ",\n" + padding.toString(), "\n", expected);
//            description.appendValue(expectedMessage);
        }

        @Override
        protected boolean matchesSafely(GeneratedMessageV3 actual, Description mismatchDescription) {
            Map<String, List<Object>> actualEnvelope = openEnvelope(singletonList(actual));

            for (Matcher<?> matcher : expected) {
                if (!matcher.matches(actualEnvelope)) {
//                    mismatchDescription = new StringDescription();
                    matcher.describeMismatch(actualEnvelope, mismatchDescription);
                    return false;
                }
            }
            return true;
        }

        private static List<Matcher<? super Object>> aComparableElement(List<?> expectedMessages, int depth) {
            return expectedMessages.stream()
                .map(element -> {
                    if (element instanceof GeneratedMessageV3) {
                        GeneratedMessageV3 message = (GeneratedMessageV3) element;
                        return new GeneratedMessageV3TypeSafeDiagnosingMatcher(message, depth);
                    }
                    if (element instanceof List) {
                        List<?> list = (List<?>) element;
                        return containsInRelativeOrder(aComparableElement(list, depth));
                    }

                    if (element instanceof EnumValueDescriptor) {
                        return new IsEnumValueDescriptor((EnumValueDescriptor) element);
                    }

                    if (element instanceof ByteString) {
                        return new IsByteString((ByteString) element);
                    }

                    if (element instanceof String || element instanceof Integer || element instanceof Boolean) {
                        return CoreMatchers.is(element);
                    }

                    throw new IllegalArgumentException("Unsupported type " + element.getClass() + ": " + element);
                })
                .map(matcher -> (Matcher<? super Object>) matcher)
                .collect(Collectors.toList());
        }

        private static List<Matcher<? super String>> aUriEndingWith(List<?> expectedMessages) {
            return expectedMessages.stream()
                .map(String.class::cast)
                .map(CoreMatchers::endsWith)
                .collect(Collectors.toList());
        }

        private static List<Matcher<? super String>> anId(List<?> expectedMessages) {
            return expectedMessages.stream()
                // id generation is not predictable
                .map(m -> isA(String.class))
                .collect(Collectors.toList());
        }

        private static List<Matcher<? super Messages.Timestamp>> aTimeStamp(List<?> expectedMessages) {
            return expectedMessages.stream()
                // timestamps are not predictable
                .map(m -> isA(Messages.Timestamp.class))
                .collect(Collectors.toList());
        }

        private static List<Matcher<? super Messages.Duration>> aDuration(List<?> expectedMessages) {
            return expectedMessages.stream()
                // timestamps are not predictable
                .map(m -> isA(Messages.Duration.class))
                .collect(Collectors.toList());
        }
    }

}
