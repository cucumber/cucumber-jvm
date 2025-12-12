package io.cucumber.core.snippets;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GherkinKeywordNormalizerTest {

    @ParameterizedTest
    @CsvSource({
            "en, G I V E N, GIVEN",
            "en, Given', Given",
            "en, Hyphen\u00ADated, Hyphenated",
            "fr, Sc\u0065\u0301nario, Scénario",
            "ar, مثا ل ,مثال",
            "em, \uD83C\uDFE6, Bank",
            "em, ✅, WhiteHeavyCheckMark"
    })
    void shouldNormalizeKeyword(String language, String keyword, String expected) {
        String normalizedKeyword = GherkinKeywordNormalizer.normalizeKeyword(language, keyword);
        assertEquals(expected, normalizedKeyword);
    }

    @ParameterizedTest
    @CsvSource({
            "En US, en_us",
            "en-tx, en_tx",
            "AR, ar",
            "cy-GB, cy_gb"
    })
    void normalizeLanguage(String language, String expected) {
        String normalizedLanguage = GherkinKeywordNormalizer.normalizeLanguage(language);
        assertEquals(expected, normalizedLanguage);
    }
}
