package io.cucumber.core.codegen;

import org.apiguardian.api.API;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

@API(status = API.Status.INTERNAL)
public final class GherkinKeywordNormalizer {

    private GherkinKeywordNormalizer() {
        /* no-op */
    }

    public static String normalizeKeyword(String language, String keyword) {
        // Exception: Use the symbol names for the Emoj language.
        // Emoji are not legal identifiers in Java.
        if ("em".equals(language)) {
            return normalizeEmojiKeyword(keyword);
        }
        return normalizeKeyword(keyword);
    }

    public static String normalizeLanguage(String language) {
        return language.replaceAll("[\\s-]", "_").toLowerCase();
    }

    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static String normalizeKeyword(String keyword) {
        return normalize(keyword.replaceAll("[\\s',!\u00AD’]", ""));
    }

    private static String normalizeEmojiKeyword(String keyword) {
        String titleCasedName = getCodePoints(keyword).mapToObj(Character::getName)
                .map(s -> s.split(" "))
                .flatMap(Arrays::stream)
                .map(String::toLowerCase)
                .map(GherkinKeywordNormalizer::capitalize)
                .collect(joining(" "));
        return normalizeKeyword(titleCasedName);
    }

    private static String normalize(CharSequence s) {
        return Normalizer.normalize(s, Normalizer.Form.NFC);
    }

    private static IntStream getCodePoints(String s) {
        int length = s.length();
        List<Integer> codePoints = new ArrayList<>();
        for (int offset = 0; offset < length;) {
            int codepoint = s.codePointAt(offset);
            codePoints.add(codepoint);
            offset += Character.charCount(codepoint);
        }
        return codePoints.stream().mapToInt(value -> value);
    }
}
