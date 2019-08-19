package io.cucumber.core.stepexpression;


import java.lang.reflect.Type;

public class DocStringType {
    private final Type targetType;
    private final String contentType;
    private final Converter converter;

    public Converter getConverter() {
        return converter;
    }

    public String getContentType() {
        return contentType;
    }

    public Type getTargetType() {
        return targetType;
    }

    public <T> DocStringType(Type targetType, Converter<T> converter) {
        this.targetType = targetType;
        this.converter = converter;
        this.contentType = "";
    }

    public <T> DocStringType(Type targetType, String contentType, Converter<T> converter) {
        this.targetType = targetType;
        if (contentType == null) {
            this.contentType = "";
        }
        else {
            this.contentType = contentType;
        }
        this.converter = converter;
    }

    public interface Converter<T> {
        T convert(String s);
    }

}
