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

        DocStringType namedType = docStringTypeRegistry.lookUpByContentType(docString.getContentType());
        if (namedType == null) {
            DocStringType type = docStringTypeRegistry.lookUpByType(targetType);
            if (type == null) {
                throw new CucumberException(String.format("It appears you did not register docstring type for %s", type));
            }
            return (T) type.getConverter().convert(docString.getText());
        }
        return (T) namedType.getConverter().convert(docString.getText());
    }
}
