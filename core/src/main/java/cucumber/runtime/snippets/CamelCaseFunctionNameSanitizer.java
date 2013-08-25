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
            sanitized.append(capitalize(words[i].toLowerCase()));
        }

        return sanitized.toString();
    }

    private String capitalize(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }
}
