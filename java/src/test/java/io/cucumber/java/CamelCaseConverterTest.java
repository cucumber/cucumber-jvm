package io.cucumber.java;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

class CamelCaseConverterTest {

    private final CamelCaseStringConverter camelCaseConverter = new CamelCaseStringConverter();

    @ParameterizedTest
    @ValueSource(strings = {
        "testString", "TestString", "Test String",
        "test String", "Test string"
    })
    void convert_to_camel_case(String header) {
        assertThat(
            camelCaseConverter.toCamelCase(singletonMap(header, "value")),
            equalTo(singletonMap("testString", "value"))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "threeWordsString", "ThreeWordsString", "three Words String",
        "Three Words String", "Three words String", "Three Words string",
        "Three words string", "three Words string", "three words String",
        "threeWords string", "three WordsString", "three wordsString",
    })
    void convert_three_words_to_camel_case(String header) {
        assertThat(
            camelCaseConverter.toCamelCase(singletonMap(header, "value")),
            equalTo(singletonMap("threeWordsString", "value"))
        );
    }

}
