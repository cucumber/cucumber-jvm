package io.cucumber.docstring;

import io.cucumber.docstring.DocString.DocStringConverter;
import org.apiguardian.api.API;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

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

        List<DocStringType> docStringTypes = docStringTypeRegistry.lookup(docString.getContentType(), targetType);

        if (docStringTypes.isEmpty()) {
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
        if (docStringTypes.size() > 1) {
            List<String> suggestedContentTypes = suggestedContentTypes(docStringTypes);
            if (docString.getContentType() == null) {
                throw new CucumberDocStringException(format(
                    "Multiple converters found for type %s, add one of the following content types to your docstring %s",
                    targetType.getTypeName(),
                    suggestedContentTypes));
            }
            throw new CucumberDocStringException(format(
                "Multiple converters found for type %s, and the content type '%s' did not match any of the registered types %s. Change the content type of the docstring or register a docstring type for '%s'",
                targetType.getTypeName(),
                docString.getContentType(),
                suggestedContentTypes,
                docString.getContentType()));
        }

        return (T) docStringTypes.get(0).transform(docString.getContent());
    }

    private List<String> suggestedContentTypes(List<DocStringType> docStringTypes) {
        return docStringTypes.stream()
                .map(DocStringType::getContentType)
                .map(DocStringTypeRegistryDocStringConverter::emptyToAnonymous)
                .sorted()
                .distinct()
                .collect(Collectors.toList());
    }

    private static String emptyToAnonymous(String contentType) {
        return contentType.isEmpty() ? "[anonymous]" : contentType;
    }

}
