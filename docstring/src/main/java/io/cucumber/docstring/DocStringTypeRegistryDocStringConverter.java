package io.cucumber.docstring;

import io.cucumber.docstring.DocString.DocStringConverter;
import org.apiguardian.api.API;

import java.lang.reflect.Type;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@API(status = API.Status.STABLE)
public final class DocStringTypeRegistryDocStringConverter implements DocStringConverter {

    private final DocStringTypeRegistry docStringTypeRegistry;

    public DocStringTypeRegistryDocStringConverter(DocStringTypeRegistry docStringTypeRegistry) {
        this.docStringTypeRegistry = requireNonNull(docStringTypeRegistry);
    }

    @SuppressWarnings("unchecked")
    public <T> T convert(DocString docString, Type targetType) {
        if (DocString.class.equals(targetType)) {
            return (T) docString;
        }

        DocStringType docStringType = docStringTypeRegistry.lookupByContentType(docString.getContentType());
        if (docStringType != null) {
            return (T) docStringType.transform(docString.getContent());
        }

        docStringType = docStringTypeRegistry.lookupByType(targetType);
        if (docStringType != null) {
            return (T) docStringType.transform(docString.getContent());
        }

        if (docString.getContentType() == null) {
            throw new CucumberDocStringException(format(
                "It appears you did not register docstring type for %s",
                targetType.getTypeName()));
        }

        throw new CucumberDocStringException(format(
            "It appears you did not register docstring type for '%s' or %s",
            docString.getContentType(),
            targetType.getTypeName()));
    }

}
