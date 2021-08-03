package io.cucumber.compatibility.matchers;

import io.cucumber.messages.internal.com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.messages.internal.com.fasterxml.jackson.databind.node.ArrayNode;
import io.cucumber.messages.internal.com.fasterxml.jackson.databind.node.BooleanNode;
import io.cucumber.messages.internal.com.fasterxml.jackson.databind.node.NumericNode;
import io.cucumber.messages.internal.com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.messages.internal.com.fasterxml.jackson.databind.node.TextNode;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;

public class AComparableMessage extends
        TypeSafeDiagnosingMatcher<JsonNode> {

    private final List<Matcher<?>> expectedFields;
    private final int depth;

    public AComparableMessage(JsonNode expectedMessage) {
        this(expectedMessage, 0);
    }

    AComparableMessage(JsonNode expectedMessage, int depth) {
        this.depth = depth + 1;
        this.expectedFields = extractExpectedFields(expectedMessage, this.depth);
    }

    private static List<Matcher<?>> extractExpectedFields(JsonNode
            expectedMessage, int depth) {
        List<Matcher<?>> expected = new ArrayList<>();
        asMapOfJsonNameToField(expectedMessage).forEach((fieldName, expectedValue) ->
        {
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
                    expected.add(hasEntry(is(fieldName), isA(TextNode.class)));
                    break;
                // exception: protocolVersion can vary
                case "protocolVersion":
                    expected.add(hasEntry(is(fieldName), isA(TextNode.class)));
                    break;
                case "astNodeIds":
                case "stepDefinitionIds":
                    expected.add(hasEntry(is(fieldName),
                            containsInRelativeOrder(isA(TextNode.class))));
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
                            anyOf(not(hasKey(is(fieldName))), hasEntry(is(fieldName),
                                    isA(expectedValue.getClass()))));
                    break;
                default:
                    expected.add(hasEntry(is(fieldName), aComparableValue(expectedValue,
                            depth)));
            }
        });
        return expected;
    }

    @SuppressWarnings("unchecked")
    private static Matcher<?> aComparableValue(Object value, int depth) {
        if (value instanceof ObjectNode) {
            JsonNode message = (JsonNode) value;
            return new AComparableMessage(message, depth);
        }

        if (value instanceof ArrayNode) {
            ArrayNode values = (ArrayNode) value;
            Spliterator<JsonNode> spliterator = spliteratorUnknownSize(values.iterator(), 0);
            List<Matcher<? super Object>> allComparableValues = stream(spliterator, false)
                    .map(o -> aComparableValue(o, depth))
                    .map(o -> (Matcher<? super Object>) o)
                    .collect(Collectors.toList());
            return contains(allComparableValues);
        }

//        if (value instanceof Descriptors.EnumValueDescriptor) {
//            return new IsEnumValueDescriptor((Descriptors.EnumValueDescriptor) value);
//        }
//
//        if (value instanceof ByteString) {
//            return new IsByteString((ByteString) value);
//        }

        if (value instanceof TextNode
                || value instanceof NumericNode
                || value instanceof BooleanNode) {
            return CoreMatchers.is(value);
        }
        throw new IllegalArgumentException("Unsupported type " + value.getClass() +
                ": " + value);
    }

    @Override
    public void describeTo(Description description) {
        StringBuilder padding = new StringBuilder();
        for (int i = 0; i < depth + 1; i++) {
            padding.append("\t");
        }
        description.appendList("\n" + padding, ",\n" + padding,
                "\n", expectedFields);
    }

    @Override
    protected boolean matchesSafely(JsonNode actual, Description
            mismatchDescription) {
        Map<String, Object> actualFields = asMapOfJsonNameToField(actual);
        for (Matcher<?> expectedField : expectedFields) {
            if (!expectedField.matches(actualFields)) {
                expectedField.describeMismatch(actualFields, mismatchDescription);
                return false;
            }
        }
        return true;
    }

    private static Map<String, Object> asMapOfJsonNameToField(JsonNode envelope) {
        Map<String, Object> map = new LinkedHashMap<>();
        envelope.fieldNames()
                .forEachRemaining(jsonField -> {
                    JsonNode value = envelope.get(jsonField);
                    map.put(jsonField, value);
                });
        return map;
    }

}
