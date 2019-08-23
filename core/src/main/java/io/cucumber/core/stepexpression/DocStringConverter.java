package io.cucumber.core.stepexpression;

import io.cucumber.core.exception.CucumberException;
import java.lang.reflect.Type;

final class DocStringConverter {

    private final DocStringTypeRegistry docStringTypeRegistry;

    DocStringConverter(DocStringTypeRegistry docStringTypeRegistry) {
        this.docStringTypeRegistry = docStringTypeRegistry;
    }

    <T> T convert(DocString docString, Type targetType) {
        if (targetType.equals(DocString.class)) {
            return (T) docString;
        }
        DocStringType docStringType = docStringTypeRegistry.lookUpByContentType(docString.getContentType());
        if (docStringType == null) {
            if (targetType.equals(String.class)) {
                docStringType = docStringTypeRegistry.lookUpByContentType("");
            }
            else {
                throw new CucumberException(String.format("It appears you did not register docstring type for content type %s", docString.getContentType()));
            }
        }
        return (T) docStringType.getConverter().convert(docString.getText());
    }
}
