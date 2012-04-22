package cucumber.runtime.java;

import cucumber.runtime.snippets.Snippet;

import java.util.List;

public class JavaSnippet implements Snippet {

    private static final Character SAFE_START_CHAR = '_';

    @Override
    public String arguments(List<Class<?>> argumentTypes) {
        StringBuilder sb = new StringBuilder();
        int n = 1;
        for (Class<?> argType : argumentTypes) {
            if (n > 1) {
                sb.append(", ");
            }
            sb.append(argType.getSimpleName()).append(" ").append("arg").append(n++);
        }
        return sb.toString();
    }

    @Override
    public String template() {
        return "@{0}(\"{1}\")\n" +
                "public void {2}({3}) '{'\n" +
                "    // {4}\n" +
                "{5}    throw new PendingException();\n" +
                "'}'\n";
    }

    @Override
    public String sanitizeFunctionName(String functionName) {
        StringBuilder sanitized = new StringBuilder();

        String trimmedFunctionName = functionName.trim();

        Character startChar = trimmedFunctionName.charAt(0);
        sanitized.append(Character.isJavaIdentifierStart(startChar) ? startChar.toString().toLowerCase() : SAFE_START_CHAR);

        boolean previousCharEndsWord = false;
        for (int i = 1; i < trimmedFunctionName.length(); i++) {
            Character nextChar = trimmedFunctionName.charAt(i);
            if (Character.isJavaIdentifierPart(nextChar)) {
                sanitized.append(previousCharEndsWord ? nextChar.toString().toUpperCase() : nextChar.toString().toLowerCase());
                previousCharEndsWord = false;
            } else {
                previousCharEndsWord = true;
            }
        }
        return sanitized.toString();
    }

    @Override
    public String tableHint() {
        return "    // For automatic conversion, change DataTable to List<YourType>\n";
    }

    @Override
    public String namedGroupStart() {
        return null;
    }

    @Override
    public String namedGroupEnd() {
        return null;
    }

    @Override
    public String escapePattern(String pattern) {
        return pattern.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
    }
}
