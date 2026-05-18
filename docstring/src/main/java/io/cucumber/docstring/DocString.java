package io.cucumber.docstring;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A doc string. For example:
 *
 * <pre>
 * """application/json
 * {
 *   "hello": "world"
 * }
 * """
 * </pre>
 * <p>
 * A doc string is either empty or contains some content. The content type is an
 * optional description of the content using a <a
 * href=https://tools.ietf.org/html/rfc2616#section-3.7>media-type</a>.
 * <p>
 * A DocString is immutable and thread safe.
 */
@API(status = API.Status.STABLE)
public final class DocString {

    private final String content;
    private final @Nullable String contentType;
    private final DocStringConverter converter;

    private DocString(String content, @Nullable String contentType, DocStringConverter converter) {
        this.content = requireNonNull(content);
        this.contentType = contentType;
        this.converter = requireNonNull(converter);
    }

    public static DocString create(String content) {
        return create(content, null);
    }

    public static DocString create(String content, @Nullable String contentType) {
        return create(content, contentType, new ConversionRequired());
    }

    public static DocString create(String content, @Nullable String contentType, DocStringConverter converter) {
        return new DocString(content, contentType, converter);
    }

    public @Nullable Object convert(Type type) {
        return converter.convert(this, type);
    }

    public String getContent() {
        return content;
    }

    public @Nullable String getContentType() {
        return contentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, contentType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DocString docString = (DocString) o;
        return content.equals(docString.content) &&
                Objects.equals(contentType, docString.contentType);
    }

    @Override
    public String toString() {
        return DocStringFormatter.builder()
                .build()
                .format(this);
    }

    public interface DocStringConverter {

        @SuppressWarnings("TypeParameterUnusedInFormals")
        <T> @Nullable T convert(DocString docString, Type targetType);

    }

}
