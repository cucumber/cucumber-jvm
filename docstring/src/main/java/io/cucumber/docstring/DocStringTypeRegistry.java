package io.cucumber.docstring;

import org.apiguardian.api.API;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@API(status = API.Status.STABLE)
public final class DocStringTypeRegistry {

    private static final Class<String> DEFAULT_TYPE = String.class;
    private static final String DEFAULT_CONTENT_TYPE = "";
    private final Map<String, Map<Type, DocStringType>> docStringTypes = new HashMap<>();

    public DocStringTypeRegistry() {
        defineDocStringType(new DocStringType(DEFAULT_TYPE, DEFAULT_CONTENT_TYPE, (String docString) -> docString));
    }

    public void defineDocStringType(DocStringType docStringType) {
        Optional<DocStringType> optionalDocStringType = find(docStringType.getContentType(), docStringType.getType());
        if (optionalDocStringType.isPresent()) {
            throw createDuplicateTypeException(optionalDocStringType.get(), docStringType);
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

    Optional<DocStringType> lookup(String contentType, Type type) {
        return Objects.isNull(contentType) ? find(type) : find(contentType, type);
    }

    boolean hasMultipleTypes(Type type) {
        int count = docStringTypes.values().stream()
                .mapToInt(typeDocStringTypeMap -> (int) typeDocStringTypeMap.keySet().stream()
                        .filter(key -> key.equals(type))
                        .count())
                .sum();
        return count > 1;
    }

    List<String> gatherContentTypesForType(Type type) {
        List<String> contentTypes = new ArrayList<>();
        for (Map.Entry<String, Map<Type, DocStringType>> entry : docStringTypes.entrySet()) {
            for (Type registeredType : entry.getValue().keySet()) {
                if (registeredType.equals(type)) {
                    contentTypes.add(emptyToAnonymous(entry.getKey()));
                }
            }
        }
        Collections.sort(contentTypes);
        return contentTypes;
    }

    private Optional<DocStringType> find(Type type) {
        return docStringTypes.values().stream()
                .flatMap(typeDocStringTypeMap -> typeDocStringTypeMap.entrySet().stream()
                        .filter(entry -> entry.getKey().equals(type))
                        .map(Map.Entry::getValue))
                .findFirst();
    }

    private Optional<DocStringType> find(String contentType, Type type) {
        Map<Type, DocStringType> docStringTypesByType = docStringTypes.get(contentType);
        if (Objects.isNull(docStringTypesByType)) {
            return empty();
        }
        return ofNullable(docStringTypesByType.get(type));
    }

    public Map<String, Map<Type, DocStringType>> getDocStringTypes() {
        return docStringTypes;
    }
}
