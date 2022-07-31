package io.cucumber.core.stepexpression;

import io.cucumber.docstring.DocString;

import static java.util.Objects.requireNonNull;

public final class DocStringArgument implements Argument {

    private final DocStringTransformer<?> docStringType;
    private final String content;
    private final String contentType;

    DocStringArgument(DocStringTransformer<?> docStringType, String content, String contentType) {
        this.docStringType = requireNonNull(docStringType);
        this.content = requireNonNull(content);
        this.contentType = contentType;
    }

    @Override
    public Object getValue() {
        return docStringType.transform(content, contentType);
    }

    @Override
    public String toString() {
        return "DocString:\n" + DocString.create(content, contentType);
    }

}
