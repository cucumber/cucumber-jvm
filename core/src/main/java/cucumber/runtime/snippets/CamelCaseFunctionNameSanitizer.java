package cucumber.runtime.snippets;

public class CamelCaseFunctionNameSanitizer implements FunctionNameSanitizer {

    public String sanitizeFunctionName(String functionName) {

        StringBuilder sanitized = new StringBuilder();

        String trimmedFunctionName = functionName.trim();

        if (!Character.isJavaIdentifierStart(trimmedFunctionName.charAt(0))) {
            sanitized.append("_");
        }

        String[] words = trimmedFunctionName.split(" ");

        sanitized.append(words[0].toLowerCase());

        for (int i = 1; i < words.length; i++) {
            sanitized.append(sanitizeWord(words[i]));
        }

        return sanitized.toString();
    }

    private String capitalize(String line) {
        return line.length() > 0 ? Character.toUpperCase(line.charAt(0)) + line.substring(1) : "";
    }

    private boolean isUpperCaseAcronym(String word) {
        if (word == null || word.length() < 2) {
            return false;
        }

        for (char c : word.toCharArray()) {
            if (Character.isLowerCase(c)) {
                return false;
            }
        }

        return true;
    }

    private String sanitizeWord(String word) {
        if (word == null) {
            return "";
        } else if (word.length() == 2 && isUpperCaseAcronym(word)) {
            return word;
        } else {
            return capitalize(isUpperCaseAcronym(word) ? word.toLowerCase() : word);
        }
    }
}
