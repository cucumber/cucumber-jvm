package io.cucumber.core.stepexpression;

import io.cucumber.docstring.DocString;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public final class DocStringArgument implements Argument {

    private final int argumentIndex;
    private final DocStringTransformer<?> transformer;
    private final String content;
    private final @Nullable String contentType;

    DocStringArgument(
            int argumentIndex, DocStringTransformer<?> transformer, String content, @Nullable String contentType
    ) {
        this.argumentIndex = argumentIndex;
        this.transformer = requireNonNull(transformer);
        this.content = requireNonNull(content);
        this.contentType = contentType;
    }

    @Override
    public @Nullable Object getValue() {
        return transformer.transform(argumentIndex, content, contentType);
    }

    @Override
    public String toString() {
        return "DocString:\n" + DocString.create(content, contentType);
    }

}
