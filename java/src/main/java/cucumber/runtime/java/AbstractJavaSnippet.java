package cucumber.runtime.java;

import cucumber.runtime.snippets.Snippet;

import java.lang.reflect.Type;
import java.util.List;

abstract class AbstractJavaSnippet implements Snippet {
    @Override
    public final String arguments(List<Type> argumentTypes) {
        StringBuilder sb = new StringBuilder();
        int n = 1;
        for (Type argType : argumentTypes) {
            if (n > 1) {
                sb.append(", ");
            }
            sb.append(getArgType(argType)).append(" ").append("arg").append(n++);
        }
        return sb.toString();
    }

    private String getArgType(Type argType) {
        if (argType instanceof Class) {
            Class cType = (Class) argType;
            return cType.getSimpleName();
        }

        // Got a better idea? Send a PR.
        return argType.toString();
    }

    @Override
    public final String tableHint() {
        return "" +
            "    // For automatic transformation, change DataTable to one of\n" +
            "    // List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or\n" +
            "    // Map<K, List<V>>. E,K,V must be a String, Integer, Float,\n" +
            "    // Double, Byte Short, Long, BigInteger or BigDecimal.\n" +
            "    //\n" +
            "    // For other transformations you can register a DataTableType\n";
    }

    @Override
    public final String escapePattern(String pattern) {
        return pattern.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
