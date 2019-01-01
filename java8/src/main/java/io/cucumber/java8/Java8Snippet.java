package io.cucumber.java8;

final class Java8Snippet extends AbstractJavaSnippet {

    @Override
    public String template() {
        return "" +
            "{0}(\"{1}\", ({3}) -> '{'\n" +
            "    // {4}\n" +
            "{5}    throw new io.cucumber.java8.api.PendingException();\n" +
            "'}');\n";
    }
}
