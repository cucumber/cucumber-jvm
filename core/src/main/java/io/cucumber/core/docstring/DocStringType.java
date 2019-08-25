package io.cucumber.core.docstring;


import io.cucumber.core.exception.CucumberException;
import org.apiguardian.api.API;

import java.lang.reflect.Type;

import static java.util.Objects.requireNonNull;

@API(status = API.Status.STABLE)
public final class DocStringType {
    private final Type type;
    private final String contentType;
    private final Transformer transformer;

    String getContentType() {
        return contentType;
    }

    Type getType() {
        return type;
    }

    public <T> DocStringType(Type type, String contentType, Transformer<T> transformer) {
        this.type = requireNonNull(type);
        this.contentType = requireNonNull(contentType);
        this.transformer = requireNonNull(transformer);
    }

    public Object transform(String text) {
        try {
            return transformer.transform(text);
        } catch (Throwable throwable) {
            throw new CucumberException(String.format(
                "'%s' could not transform%n%s", contentType, DocString.create(text, contentType)), throwable);
        }
    }

    @FunctionalInterface
    public interface Transformer<T> {
        T transform(String s) throws Throwable;
    }

}
