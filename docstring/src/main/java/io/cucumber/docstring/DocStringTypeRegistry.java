package io.cucumber.docstring;

import org.apiguardian.api.API;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        DocStringType existing = lookupByContentTypeAndType(docStringType.getContentType(), docStringType.getType());
        if (existing != null) {
            throw createDuplicateTypeException(existing, docStringType);
        }
        Map<Type, DocStringType> map = docStringTypes.computeIfAbsent(docStringType.getContentType(),
            s -> new HashMap<>());
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

    List<DocStringType> lookup(String contentType, Type type) {
        DocStringType docStringType = lookupByContentTypeAndType(orDefault(contentType), type);
        if (docStringType != null) {
            return Collections.singletonList(docStringType);
        }

        return lookUpByType(type);
    }

    private String orDefault(String contentType) {
        return contentType == null ? DEFAULT_CONTENT_TYPE : contentType;
    }

    private List<DocStringType> lookUpByType(Type type) {
        return docStringTypes.values().stream()
                .flatMap(typeDocStringTypeMap -> typeDocStringTypeMap.entrySet().stream()
                        .filter(entry -> entry.getKey().equals(type))
                        .map(Map.Entry::getValue))
                .collect(Collectors.toList());
    }

    private DocStringType lookupByContentTypeAndType(String contentType, Type type) {
        Map<Type, DocStringType> docStringTypesByType = docStringTypes.get(contentType);
        if (docStringTypesByType == null) {
            return null;
        }
        return docStringTypesByType.get(type);
    }
}
