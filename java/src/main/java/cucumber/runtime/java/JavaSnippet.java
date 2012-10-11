package cucumber.runtime.java;

import cucumber.runtime.snippets.Snippet;

import java.util.List;

class JavaSnippet implements Snippet {

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
                "public void {2}({3}) throws Throwable '{'\n" +
                "    // {4}\n" +
                "{5}    throw new PendingException();\n" +
                "'}'\n";
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
        return pattern.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
