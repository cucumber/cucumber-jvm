package io.cucumber.core.runner;

import io.cucumber.core.exception.CucumberException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CamelCaseConverterTest {

    private final CamelCaseStringConverter camelCaseConverter = new CamelCaseStringConverter();

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "testString", "TestString", "Test String",
                    "test String", "Test string"
            })
    void convert_to_camel_case(String header) {
        assertThat(
            camelCaseConverter.toCamelCase(singletonMap(header, "value")),
            equalTo(singletonMap("testString", "value")));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "threeWordsString", "ThreeWordsString", "three Words String",
                    "Three Words String", "Three words String", "Three Words string",
                    "Three words string", "three Words string", "three words String",
                    "threeWords string", "three WordsString", "three wordsString",
            })
    void convert_three_words_to_camel_case(String header) {
        assertThat(
            camelCaseConverter.toCamelCase(singletonMap(header, "value")),
            equalTo(singletonMap("threeWordsString", "value")));
    }

    @Test
    void should_throw_on_duplicate_headers() {
        Map<String, String> table = new HashMap<>();
        table.put("Title Case Header", "value1");
        table.put("TitleCaseHeader", "value2");

        CucumberException exception = assertThrows(
            CucumberException.class,
            () -> camelCaseConverter.toCamelCase(table));
        assertThat(exception.getMessage(), is("" +
                "Failed to convert header 'Title Case Header' to property name. " +
                "'TitleCaseHeader' also converted to 'titleCaseHeader'"));
    }

}
