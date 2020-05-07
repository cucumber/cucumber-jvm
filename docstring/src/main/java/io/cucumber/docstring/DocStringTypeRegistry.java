package io.cucumber.docstring;

import org.apiguardian.api.API;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

@API(status = API.Status.STABLE)
public final class DocStringTypeRegistry {

    private final Map<String, DocStringType> docStringTypesByContentType = new HashMap<>();
    private final Map<Type, DocStringType> docStringTypesByType = new HashMap<>();

    public DocStringTypeRegistry() {
        defineDocStringType(new DocStringType(String.class, "", (String docString) -> docString));
    }

    public void defineDocStringType(DocStringType docStringType) {
        DocStringType byContentType = docStringTypesByContentType.get(docStringType.getContentType());
        if (byContentType != null) {
            throw createDuplicateTypeException(byContentType, docStringType);
        }
        DocStringType byClass = docStringTypesByType.get(docStringType.getType());
        if (byClass != null) {
            throw createDuplicateTypeException(byClass, docStringType);

        }
        docStringTypesByContentType.put(docStringType.getContentType(), docStringType);
        docStringTypesByType.put(docStringType.getType(), docStringType);
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

    DocStringType lookupByContentType(String contentType) {
        return docStringTypesByContentType.get(contentType);
    }

    DocStringType lookupByType(Type type) {
        return docStringTypesByType.get(type);
    }

}
