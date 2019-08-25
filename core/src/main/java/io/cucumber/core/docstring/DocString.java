package io.cucumber.core.docstring;

import org.apiguardian.api.API;

import java.lang.reflect.Type;
import java.util.Objects;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

@API(status = API.Status.STABLE)
public final class DocString {
    private final String text;
    private final String contentType;
    private final DocStringConverter converter;

    public static DocString create(String text, String contentType, DocStringConverter converter) {
        return new DocString(text, contentType, converter);
    }

    public static DocString create(String docString, String contentType) {
        return create(docString, contentType, new ConversionRequired());
    }

    public static DocString create(String docString) {
        return create(docString, null);
    }

    public Object convert(Type type) {
        return converter.convert(this, type);
    }

    public String getText() {
        return text;
    }

    public String getContentType() {
        return contentType;
    }

    private DocString(String text, String contentType, DocStringConverter converter) {
        this.text = requireNonNull(text);
        this.contentType = contentType;
        this.converter = requireNonNull(converter);
    }

    public interface DocStringConverter {
        <T> T convert(DocString docString, Type targetType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocString docString = (DocString) o;
        return text.equals(docString.text) &&
            contentType.equals(docString.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, contentType);
    }

    @Override
    public String toString() {
        return stream(text.split("\n"))
            .collect(joining(
                "\n      ",
                "      \"\"\"" + contentType + "\n      ",
                "\n      \"\"\""
            ));
    }
}
