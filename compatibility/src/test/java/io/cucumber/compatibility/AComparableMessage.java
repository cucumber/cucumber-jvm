package io.cucumber.compatibility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.stream.Collectors;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsIterableContainingInRelativeOrder.containsInRelativeOrder;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;

public class AComparableMessage extends
        TypeSafeDiagnosingMatcher<JsonNode> {

    private final List<Matcher<?>> expectedFields;
    private final int depth;

    public AComparableMessage(String messageType, JsonNode expectedMessage) {
        this(messageType, expectedMessage, 0);
    }

    AComparableMessage(String messageType, JsonNode expectedMessage, int depth) {
        this.depth = depth + 1;
        this.expectedFields = extractExpectedFields(messageType, expectedMessage, this.depth);
    }

    private static List<Matcher<?>> extractExpectedFields(String messageType, JsonNode expectedMessage, int depth) {
        List<Matcher<?>> expected = new ArrayList<>();
        asMapOfJsonNameToField(expectedMessage).forEach((fieldName, expectedValue) -> {
            switch (fieldName) {
                // exception: error messages are platform specific
                case "exception":
                case "message":
                    expected.add(hasEntry(is(fieldName), isA(expectedValue.getClass())));
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
                    // exception: not yet implemented
                    if ("testRunStarted".equals(messageType)) {
                        expected.add(not(hasKey(fieldName)));
                        break;
                    }
                case "pickleId":
                case "astNodeId":
                case "hookId":
                case "pickleStepId":
                case "testCaseId":
                case "testStepId":
                case "testCaseStartedId":
                    expected.add(hasEntry(is(fieldName), isA(TextNode.class)));
                    break;
                // exception: not yet implemented
                case "testRunStartedId":
                    expected.add(not(hasKey(fieldName)));
                    break;
                // exception: protocolVersion can vary
                case "protocolVersion":
                    expected.add(hasEntry(is(fieldName), isA(TextNode.class)));
                    break;
                case "astNodeIds":
                case "stepDefinitionIds":
                    if (expectedValue instanceof ArrayNode) {
                        ArrayNode expectedValues = (ArrayNode) expectedValue;
                        if (expectedValues.isEmpty()) {
                            expected.add(hasEntry(is(fieldName), emptyIterable()));
                        } else {
                            expected.add(hasEntry(is(fieldName), containsInRelativeOrder(isA(TextNode.class))));
                        }
                        break;
                    }
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
                    expected.add(hasEntry(is(fieldName), aComparableValue(messageType,
                        expectedValue,
                        depth)));
            }
        });
        return expected;
    }

    @SuppressWarnings("unchecked")
    private static Matcher<?> aComparableValue(String messageType, Object value, int depth) {
        if (value instanceof ObjectNode) {
            JsonNode message = (JsonNode) value;
            return new AComparableMessage(messageType, message, depth);
        }

        if (value instanceof ArrayNode) {
            ArrayNode values = (ArrayNode) value;
            Spliterator<JsonNode> spliterator = spliteratorUnknownSize(values.iterator(), 0);
            List<Matcher<? super Object>> allComparableValues = stream(spliterator, false)
                    .map(o -> aComparableValue(messageType, o, depth))
                    .map(o -> (Matcher<? super Object>) o)
                    .collect(Collectors.toList());
            if (allComparableValues.isEmpty()) {
                return emptyIterable();
            }
            return contains(allComparableValues);
        }

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
    protected boolean matchesSafely(JsonNode actual, Description mismatchDescription) {
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
