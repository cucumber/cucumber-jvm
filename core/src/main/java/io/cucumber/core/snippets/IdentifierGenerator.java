package io.cucumber.core.snippets;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Character.isJavaIdentifierStart;

final class IdentifierGenerator {

    private static final String BETWEEN_LOWER_AND_UPPER = "(?<=\\p{Ll})(?=\\p{Lu})";
    private static final String BEFORE_UPPER_AND_LOWER = "(?<=\\p{L})(?=\\p{Lu}\\p{Ll})";
    private static final Pattern SPLIT_CAMEL_CASE = Pattern
            .compile(BETWEEN_LOWER_AND_UPPER + "|" + BEFORE_UPPER_AND_LOWER);
    private static final Pattern SPLIT_WHITESPACE = Pattern.compile("\\s");
    private static final Pattern SPLIT_UNDERSCORE = Pattern.compile("_");

    private static final char SUBST = ' ';
    private final Joiner joiner;

    IdentifierGenerator(Joiner joiner) {
        this.joiner = joiner;
    }

    String generate(String sentence) {
        if (sentence.isEmpty()) {
            throw new IllegalArgumentException("Cannot create function name from empty sentence");
        }

        List<String> words = Stream.of(sentence)
                .map(this::replaceIllegalCharacters)
                .map(String::trim)
                .flatMap(SPLIT_WHITESPACE::splitAsStream)
                .flatMap(SPLIT_CAMEL_CASE::splitAsStream)
                .flatMap(SPLIT_UNDERSCORE::splitAsStream)
                .collect(Collectors.toList());

        return joiner.concatenate(words);
    }

    private String replaceIllegalCharacters(String sentence) {
        StringBuilder sanitized = new StringBuilder();
        sanitized.append(isJavaIdentifierStart(sentence.charAt(0)) ? sentence.charAt(0) : SUBST);
        for (int i = 1; i < sentence.length(); i++) {
            if (Character.isJavaIdentifierPart(sentence.charAt(i))) {
                sanitized.append(sentence.charAt(i));
            } else if (sanitized.charAt(sanitized.length() - 1) != SUBST && i != sentence.length() - 1) {
                sanitized.append(SUBST);
            }
        }
        return sanitized.toString();
    }

}
