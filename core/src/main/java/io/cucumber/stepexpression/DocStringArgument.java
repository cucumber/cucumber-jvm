package io.cucumber.stepexpression;

public final class DocStringArgument implements Argument {

    private final DocStringTransformer<?> docStringType;
    private final String argument;

    DocStringArgument(DocStringTransformer<?> docStringType, String argument) {
        this.docStringType = docStringType;
        this.argument = argument;
    }

    @Override
    public Object getValue() {
        return docStringType.transform(argument);
    }

    @Override
    public String toString() {
        return "DocString: " + argument;
    }
}
