package io.cucumber.java;

final class Java8Snippet extends AbstractJavaSnippet {

    @Override
    public String template() {
        return "" +
            "{0}(\"{1}\", ({3}) -> '{'\n" +
            "    // {4}\n" +
            "{5}    throw new io.cucumber.java.api.PendingException();\n" +
            "'}');\n";
    }
}
