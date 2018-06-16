package cucumber.runtime.java;

final class JavaSnippet extends AbstractJavaSnippet {

    @Override
    public String template() {
        return "" +
            "@{0}(\"{1}\")\n" +
            "public void {2}({3}) '{'\n" +
            "    // {4}\n" +
            "{5}    throw new cucumber.api.PendingException();\n" +
            "'}'\n";
    }
}
