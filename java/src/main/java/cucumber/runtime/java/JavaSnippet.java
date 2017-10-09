package cucumber.runtime.java;

class JavaSnippet extends AbstractJavaSnippet {

    @Override
    protected String getArgType(Class<?> argType) {
        return argType.getSimpleName();
    }

    @Override
    public String template() {
        return "@{0}(\"{1}\")\n" +
                "public void {2}({3}) throws Exception '{'\n" +
                "    // {4}\n" +
                "{5}    throw new PendingException();\n" +
                "'}'\n";
    }
}
