package io.cucumber.docstring;

import org.apiguardian.api.API;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;

@API(status = API.Status.STABLE)
public final class DocStringTypeRegistry {

    private static final Class<String> DEFAULT_TYPE = String.class;
    private static final String DEFAULT_CONTENT_TYPE = "";
    private final Map<String, Map<Type, DocStringType>> docStringTypes = new HashMap<>();

    public DocStringTypeRegistry() {
        defineDocStringType(new DocStringType(DEFAULT_TYPE, DEFAULT_CONTENT_TYPE, (String docString) -> docString));
    }

    public void defineDocStringType(DocStringType docStringType) {
        if (docStringTypes.containsKey(docStringType.getContentType()) &&
                docStringTypes.get(docStringType.getContentType()).containsKey(docStringType.getType())) {
            throw createDuplicateTypeException(
                docStringTypes.get(docStringType.getContentType()).get(docStringType.getType()),
                docStringType);
        }
        Map<Type, DocStringType> map;
        if (docStringTypes.containsKey(docStringType.getContentType())) {
            map = docStringTypes.get(docStringType.getContentType());
        } else {
            map = new HashMap<>();
        }
        map.put(docStringType.getType(), docStringType);
        docStringTypes.put(docStringType.getContentType(), map);
    }

    private static CucumberDocStringException createDuplicateTypeException(
            DocStringType existing, DocStringType docStringType
    ) {
        String contentType = existing.getContentType();
        return new CucumberDocStringException(format("" +
                "There is already docstring type registered for '%s' and %s.\n" +
                "You are trying to add '%s' and %s",
            emptyToAnonymous(contentType),
            existing.getType().getTypeName(),
            emptyToAnonymous(docStringType.getContentType()),
            docStringType.getType().getTypeName()));
    }

    private static String emptyToAnonymous(String contentType) {
        return contentType.isEmpty() ? "[anonymous]" : contentType;
    }

    Optional<DocStringType> lookup(String contentType, Type type) {
        if (Objects.isNull(contentType)) {
            if (docStringTypes.get(DEFAULT_CONTENT_TYPE).containsKey(type)) {
                return Optional.of(docStringTypes.get(DEFAULT_CONTENT_TYPE).get(type));
            }
        }
        if (docStringTypes.containsKey(contentType) &&
                docStringTypes.get(contentType).containsKey(type)) {
            return Optional.of(docStringTypes.get(contentType).get(type));
        }
        return Optional.empty();
    }
}
