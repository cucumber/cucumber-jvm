package io.cucumber.compatibility;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

final class AComparableMessage extends TypeSafeDiagnosingMatcher<JsonNode> {

    private final JsonNode expectedMessage;
    private final String messageType;
    private final Map<Pattern, Matcher<?>> replacements;
    private final Map<JsonPointer, JsonNode> expectedFields;
    private final Map<JsonPointer, Matcher<?>> expectedMatchers;

    AComparableMessage(String messageType, JsonNode expectedMessage, Map<Pattern, Matcher<?>> replacements) {
        this.expectedMessage = expectedMessage;
        this.messageType = requireNonNull(messageType);
        this.replacements = requireNonNull(replacements);
        this.expectedFields = extractFieldsAndPointers(requireNonNull(expectedMessage));
        this.expectedMatchers = createMatchers(expectedFields);
    }

    private Map<JsonPointer, Matcher<?>> createMatchers(Map<JsonPointer, JsonNode> expectedFields) {
        Map<JsonPointer, Matcher<?>> expectedMatchers = new LinkedHashMap<>();
        expectedFields.forEach((jsonPointer, node) -> {
            Matcher<JsonNode> defaultValue = CoreMatchers.equalTo(node);
            expectedMatchers.put(jsonPointer, findReplacement(jsonPointer, defaultValue));
        });
        return expectedMatchers;
    }

    private Matcher<?> findReplacement(JsonPointer jsonPointer, Matcher<JsonNode> defaultValue) {
        for (Map.Entry<Pattern, Matcher<?>> entry : replacements.entrySet()) {
            if (entry.getKey().matcher(jsonPointer.toString()).matches()) {
                return entry.getValue();
            }
        }
        return defaultValue;
    }

    private Map<JsonPointer, JsonNode> extractFieldsAndPointers(JsonNode node) {
        JsonPointer path = JsonPointer.empty();
        return extractFieldsAndPointers(path, node);
    }

    private Map<JsonPointer, JsonNode> extractFieldsAndPointers(JsonPointer path, JsonNode node) {
        if (node instanceof ObjectNode jsonNodes) {
            return extractFieldsAndPointers(path, jsonNodes);
        }
        if (node instanceof ArrayNode jsonNodes) {
            return extractFieldsAndPointers(path, jsonNodes);
        }
        return Collections.singletonMap(path, node);
    }

    private Map<JsonPointer, JsonNode> extractFieldsAndPointers(JsonPointer path, ObjectNode node) {
        Map<JsonPointer, JsonNode> expectedFields = new LinkedHashMap<>();
        node.fieldNames().forEachRemaining(fieldName -> {
            JsonNode field = node.get(fieldName);
            JsonPointer fieldPath = path.appendProperty(fieldName);
            expectedFields.putAll(extractFieldsAndPointers(fieldPath, field));
        });
        return expectedFields;
    }

    private Map<JsonPointer, JsonNode> extractFieldsAndPointers(JsonPointer path, ArrayNode node) {
        Map<JsonPointer, JsonNode> expectedFields = new LinkedHashMap<>();
        for (int i = 0, size = node.size(); i < size; i++) {
            JsonNode element = node.get(i);
            JsonPointer elementPath = path.appendIndex(i);
            expectedFields.putAll(extractFieldsAndPointers(elementPath, element));
        }
        return expectedFields;
    }

    @Override
    protected boolean matchesSafely(JsonNode item, Description mismatchDescription) {
        for (Map.Entry<JsonPointer, Matcher<?>> entry : expectedMatchers.entrySet()) {
            JsonPointer pointer = entry.getKey();
            Matcher<?> expected = entry.getValue();
            JsonNode actual = item.at(pointer);

            if (!expected.matches(actual)) {
                mismatchDescription
                        .appendText(pointer.toString()).appendText(" ")
                        .appendText(actual.toString()).appendText(" ");
                // Copy and paste needed to suppress this finding.
                System.out.printf("%s.put(Pattern.compile(\"%s\"),  isA(%s.class));%n", messageType, pointer,
                    actual.getClass().getSimpleName());
                return false;
            }
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(expectedMessage);
    }

    @Override
    public String toString() {
        return "AComparableMessage{" +
                "expectedMessage=" + expectedMessage +
                ", messageType='" + messageType + '\'' +
                ", replacements=" + replacements +
                ", expectedFields=" + expectedFields +
                ", expectedMatchers=" + expectedMatchers +
                '}';
    }
}
