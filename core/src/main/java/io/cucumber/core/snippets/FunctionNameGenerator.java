package io.cucumber.core.snippets;

final class FunctionNameGenerator {
    private static final Character SUBST = ' ';
    private final Joiner joiner;

    FunctionNameGenerator(Joiner joiner) {
        this.joiner = joiner;
    }

    String generateFunctionName(String sentence) {

        sentence = removeIllegalCharacters(sentence);
        sentence = sentence.trim();
        String[] words = sentence.split("\\s");

        return joiner.concatenate(words);
    }

    private String removeIllegalCharacters(String sentence) {
        if (sentence.isEmpty()) {
            throw new IllegalArgumentException("Cannot create function name from empty sentence");
        }
        StringBuilder sanitized = new StringBuilder();
        sanitized.append(Character.isJavaIdentifierStart(sentence.charAt(0)) ? sentence.charAt(0) : SUBST);
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
