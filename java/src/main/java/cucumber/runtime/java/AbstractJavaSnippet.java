package cucumber.runtime.java;

import cucumber.runtime.snippets.Snippet;

import java.util.List;

public abstract class AbstractJavaSnippet implements Snippet {
    @Override
    public String arguments(List<Class<?>> argumentTypes) {
        StringBuilder sb = new StringBuilder();
        int n = 1;
        for (Class<?> argType : argumentTypes) {
            if (n > 1) {
                sb.append(", ");
            }
            sb.append(getArgType(argType)).append(" ").append("arg").append(n++);
        }
        return sb.toString();
    }

    protected abstract String getArgType(Class<?> argType);

    @Override
    public String tableHint() {
        return "    // For automatic transformation, change DataTable to one of\n" +
                "    // List<YourType>, List<List<E>>, List<Map<K,V>> or Map<K,V>.\n" +
                "    // E,K,V must be a scalar (String, Integer, Date, enum etc)\n";
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
        return pattern.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
