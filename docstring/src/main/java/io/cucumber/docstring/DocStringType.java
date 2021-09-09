package io.cucumber.docstring;

import org.apiguardian.api.API;

import java.lang.reflect.Type;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * A data table type describes how a doc string should be represented as an
 * object.
 */
@API(status = API.Status.STABLE)
public final class DocStringType {

    private final Type type;
    private final String contentType;
    private final Transformer<?> transformer;

    /**
     * Creates a doc string type that can convert a doc string to an object.
     *
     * @param type        the type of the object
     * @param contentType the <a href=
     *                    "https://www.iana.org/assignments/media-types/media-types.xhtml">media
     *                    type</a> or <a href=
     *                    "https://github.github.com/gfm/#info-string">GFM info
     *                    string</a>
     * @param transformer a function that creates an instance of
     *                    <code>type</code> from the doc string
     * @param <T>         see <code>type</code>
     */
    public <T> DocStringType(Type type, String contentType, Transformer<T> transformer) {
        this.type = requireNonNull(type);
        this.contentType = requireNonNull(contentType);
        this.transformer = requireNonNull(transformer);
    }

    String getContentType() {
        return contentType;
    }

    Type getType() {
        return type;
    }

    Object transform(String content) {
        try {
            return transformer.transform(content);
        } catch (Throwable throwable) {
            throw new CucumberDocStringException(format(
                "'%s' could not transform%n%s",
                contentType, DocString.create(content, contentType)),
                throwable);
        }
    }

    @FunctionalInterface
    public interface Transformer<T> {

        T transform(String content) throws Throwable;

    }

}
