package io.cucumber.docstring;

import io.cucumber.docstring.DocString.DocStringConverter;
import org.apiguardian.api.API;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

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

        if (Objects.isNull(docString.getContentType()) &&
                docStringTypeRegistry.hasMultipleTypes(targetType)) {
            throw new CucumberDocStringException(format(
                "Multiple converters found for type %s, add one of the following content types to docstring %s",
                targetType.getTypeName(),
                docStringTypeRegistry.gatherContentTypesForType(targetType)));
        }

        Optional<DocStringType> lookup = docStringTypeRegistry.lookup(docString.getContentType(), targetType);
        if (lookup.isPresent()) {
            return (T) lookup.get().transform(docString.getContent());
        }

        if (Objects.isNull(docString.getContentType())) {
            throw new CucumberDocStringException(format(
                "It appears you did not register docstring type for %s",
                targetType.getTypeName()));
        } else {
            throw new CucumberDocStringException(format(
                "It appears you did not register docstring type for '%s' or %s",
                docString.getContentType(),
                targetType.getTypeName()));
        }

    }

}
