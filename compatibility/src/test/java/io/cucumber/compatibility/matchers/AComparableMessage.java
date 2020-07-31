package io.cucumber.compatibility.matchers;

import io.cucumber.messages.internal.com.google.protobuf.ByteString;
import io.cucumber.messages.internal.com.google.protobuf.Descriptors;
import io.cucumber.messages.internal.com.google.protobuf.GeneratedMessageV3;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;

public class AComparableMessage extends TypeSafeDiagnosingMatcher<GeneratedMessageV3> {

    private final List<Matcher<?>> expectedFields;
    private final int depth;

    public AComparableMessage(GeneratedMessageV3 expectedMessage) {
        this(expectedMessage, 0);
    }

    AComparableMessage(GeneratedMessageV3 expectedMessage, int depth) {
        this.depth = depth + 1;
        this.expectedFields = extractExpectedFields(expectedMessage, this.depth);
    }

    private static List<Matcher<?>> extractExpectedFields(GeneratedMessageV3 expectedMessage, int depth) {
        List<Matcher<?>> expected = new ArrayList<>();
        asMapOfJsonNameToField(expectedMessage).forEach((fieldName, expectedValue) -> {
            switch (fieldName) {
                // exception: error messages are platform specific
                case "message":
                    expected.add(hasEntry(is(fieldName), isA(expectedValue.getClass())));
                    break;

                // exception: the CCK uses relative paths as uris
                case "uri":
                    expected.add(hasEntry(is(fieldName), isA(expectedValue.getClass())));
                    break;

                // exception: the CCK expects source references with URIs but
                // Java can only provide method and stack trace references.
                case "sourceReference":
                    expected.add(hasKey(is(fieldName)));
                    break;

                // exception: ids are not predictable
                case "id":
                case "pickleId":
                case "astNodeId":
                case "hookId":
                case "pickleStepId":
                case "testCaseId":
                case "testStepId":
                case "testCaseStartedId":
                    expected.add(hasEntry(is(fieldName), isA(String.class)));
                    break;
                // exception: protocolVersion can vary
                case "protocolVersion":
                    expected.add(hasEntry(is(fieldName), isA(String.class)));
                    break;
                case "astNodeIds":
                case "stepDefinitionIds":
                    expected.add(hasEntry(is(fieldName), containsInRelativeOrder(isA(String.class))));
                    break;

                // exception: timestamps and durations are not predictable
                case "timestamp":
                case "duration":
                    expected.add(hasEntry(is(fieldName), isA(expectedValue.getClass())));
                    break;

                // exception: Mata fields depend on the platform
                case "implementation":
                case "runtime":
                case "os":
                case "cpu":
                    expected.add(hasEntry(is(fieldName), isA(expectedValue.getClass())));
                    break;
                case "ci":
                    // exception: Absent when running locally, present in ci
                    expected.add(
                        anyOf(not(hasKey(is(fieldName))), hasEntry(is(fieldName), isA(expectedValue.getClass()))));
                    break;
                default:
                    expected.add(hasEntry(is(fieldName), aComparableValue(expectedValue, depth)));
            }
        });
        return expected;
    }

    @SuppressWarnings("unchecked")
    private static Matcher<?> aComparableValue(Object value, int depth) {
        if (value instanceof GeneratedMessageV3) {
            GeneratedMessageV3 message = (GeneratedMessageV3) value;
            return new AComparableMessage(message, depth);
        }

        if (value instanceof List) {
            List<?> values = (List<?>) value;
            List<Matcher<? super Object>> allComparableValues = values.stream()
                    .map(o -> aComparableValue(o, depth))
                    .map(o -> (Matcher<? super Object>) o)
                    .collect(Collectors.toList());
            return contains(allComparableValues);
        }

        if (value instanceof Descriptors.EnumValueDescriptor) {
            return new IsEnumValueDescriptor((Descriptors.EnumValueDescriptor) value);
        }

        if (value instanceof ByteString) {
            return new IsByteString((ByteString) value);
        }

        if (value instanceof String || value instanceof Integer || value instanceof Boolean) {
            return CoreMatchers.is(value);
        }
        throw new IllegalArgumentException("Unsupported type " + value.getClass() + ": " + value);
    }

    @Override
    public void describeTo(Description description) {
        StringBuilder padding = new StringBuilder();
        for (int i = 0; i < depth + 1; i++) {
            padding.append("\t");
        }
        description.appendList("\n" + padding.toString(), ",\n" + padding.toString(), "\n", expectedFields);
    }

    @Override
    protected boolean matchesSafely(GeneratedMessageV3 actual, Description mismatchDescription) {
        Map<String, Object> actualFields = asMapOfJsonNameToField(actual);
        for (Matcher<?> expectedField : expectedFields) {
            if (!expectedField.matches(actualFields)) {
                expectedField.describeMismatch(actualFields, mismatchDescription);
                return false;
            }
        }
        return true;
    }

    private static Map<String, Object> asMapOfJsonNameToField(GeneratedMessageV3 envelope) {
        Map<String, Object> map = new LinkedHashMap<>();
        envelope.getAllFields()
                .forEach((fieldDescriptor, value) -> {
                    String jsonName = fieldDescriptor.getJsonName();
                    map.put(jsonName, value);
                });
        return map;
    }

}
