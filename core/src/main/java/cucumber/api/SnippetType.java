package cucumber.api;

public enum SnippetType {
    UNDERSCORE("underscore", new UnderscoreJoiner()),
    CAMELCASE("camelcase", new CamelCaseJoiner());

    private final String name;
    private final Joiner joiner;

    SnippetType(String name, Joiner joiner) {
        this.name = name;
        this.joiner = joiner;
    }

    public static SnippetType fromString(String name) {
        for (SnippetType snippetType : SnippetType.values()) {
            if (name.equalsIgnoreCase(snippetType.name)) {
                return snippetType;
            }
        }
        throw new IllegalArgumentException(String.format("Unrecognized SnippetType %s", name));
    }

    public FunctionNameGenerator getFunctionNameGenerator() {
        return new FunctionNameGenerator(joiner);
    }

    private static final class CamelCaseJoiner implements Joiner {

        @Override
        public String concatenate(String[] words) {
            StringBuilder functionName = new StringBuilder();
            boolean firstWord = true;
            for (String word : words) {
                if (firstWord) {
                    functionName.append(word.toLowerCase());
                    firstWord = false;
                } else {
                    functionName.append(capitalize(word));
                }
            }
            return functionName.toString();
        }

        private String capitalize(String line) {
            return Character.toUpperCase(line.charAt(0)) + line.substring(1);
        }
    }

    private static class UnderscoreJoiner implements Joiner {
        @Override
        public String concatenate(String[] words) {
            StringBuilder functionName = new StringBuilder();
            boolean firstWord = true;
            for (String word : words) {
                if (firstWord) {
                    word = word.toLowerCase();
                } else {
                    functionName.append('_');
                }
                functionName.append(word);
                firstWord = false;
            }
            return functionName.toString();
        }
    }

    private interface Joiner {
        String concatenate(String[] words);
    }

    public static final class FunctionNameGenerator {
        private static final Character SUBST = ' ';
        private final Joiner joiner;

        private FunctionNameGenerator(Joiner joiner) {
            this.joiner = joiner;
        }

        public String generateFunctionName(String sentence) {

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
}
