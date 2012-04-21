package cucumber.runtime.jython;

import cucumber.runtime.snippets.Snippet;

import java.util.List;

import static cucumber.runtime.snippets.SnippetGenerator.untypedArguments;

public class JythonSnippet implements Snippet {

    private static final Character SAFE_START_CHAR = '_';

    @Override
    public String template() {
        return "@{0}(''{1}'')\n" +
                "def {2}({3}):\n" +
                "  # {4}\n" +
                "  raise(PendingException())\n" +
                "";
    }

    @Override
    public String sanitizeFunctionName(String functionName) {
        StringBuilder sanitized = new StringBuilder();

        String trimmedFunctionName = functionName.trim();

        sanitized.append(Character.isJavaIdentifierStart(trimmedFunctionName.charAt(0)) ? trimmedFunctionName.charAt(0) : SAFE_START_CHAR);
        for (int i = 1; i < trimmedFunctionName.length(); i++) {
            if (Character.isJavaIdentifierPart(trimmedFunctionName.charAt(i))) {
                sanitized.append(trimmedFunctionName.charAt(i));
            } else if (sanitized.charAt(sanitized.length() - 1) != SAFE_START_CHAR && i != trimmedFunctionName.length() - 1) {
                sanitized.append(SAFE_START_CHAR);
            }
        }
        return sanitized.toString();
    }

    @Override
    public String tableHint() {
        return null;
    }

    @Override
    public String arguments(List<Class<?>> argumentTypes) {
        return untypedArguments(argumentTypes);
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
        return pattern;
    }
}
