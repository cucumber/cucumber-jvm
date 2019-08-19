package io.cucumber.core.stepexpression;

public final class DocStringArgument implements Argument {

    private final DocStringTransformer<?> docStringType;
    private final String argument;
    private final String contentType;

    DocStringArgument(DocStringTransformer<?> docStringType, String argument, String contentType) {
        this.docStringType = docStringType;
        this.argument = argument;
        this.contentType = contentType;
    }

    @Override
    public Object getValue() {
        return docStringType.transform(argument, contentType);
    }

    @Override
    public String toString() {
        return "DocString: " + argument;
    }
}
