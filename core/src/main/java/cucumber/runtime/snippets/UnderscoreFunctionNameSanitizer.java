package cucumber.runtime.snippets;

public class UnderscoreFunctionNameSanitizer implements FunctionNameSanitizer {
    private static final Character SUBST = '_';

    public String sanitizeFunctionName(String functionName) {
        StringBuilder sanitized = new StringBuilder();

        String trimmedFunctionName = functionName.trim();
        if (trimmedFunctionName.isEmpty()) {
            throw new IllegalArgumentException("Cannot have empty function name");
        }

        sanitized.append(Character.isJavaIdentifierStart(trimmedFunctionName.charAt(0)) ? trimmedFunctionName.charAt(0) : SUBST);
        for (int i = 1; i < trimmedFunctionName.length(); i++) {
            if (Character.isJavaIdentifierPart(trimmedFunctionName.charAt(i))) {
                sanitized.append(trimmedFunctionName.charAt(i));
            } else if (sanitized.charAt(sanitized.length() - 1) != SUBST && i != trimmedFunctionName.length() - 1) {
                sanitized.append(SUBST);
            }
        }
        return sanitized.toString();
    }
}
