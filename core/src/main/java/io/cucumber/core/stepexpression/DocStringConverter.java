package io.cucumber.core.stepexpression;

import java.lang.reflect.Type;

final class DocStringConverter {

    private final DocStringTypeRegistry docStringTypeRegistry;

    public DocStringConverter(DocStringTypeRegistry docStringTypeRegistry) {
        this.docStringTypeRegistry = docStringTypeRegistry;
    }

    <T> T convert(String docString, Type type) {
        return (T) docStringTypeRegistry.lookUpByType(type).getConverter().convert(docString);
    }

    <T> T convert(String docString, String contentType) {
        return (T) docStringTypeRegistry.lookUpByContentType(contentType).getConverter().convert(docString);
    }
}
