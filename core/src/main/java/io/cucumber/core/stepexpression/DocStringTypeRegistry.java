package io.cucumber.core.stepexpression;

import static java.lang.String.format;

import io.cucumber.core.exception.CucumberException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DocStringTypeRegistry {

    private final Map<Type, DocStringType> anonymousDocstringTypes;
    private final Map<String, DocStringType> namedDocstringTypes;

    public DocStringTypeRegistry() {
        anonymousDocstringTypes = new HashMap<>();
        namedDocstringTypes = new HashMap<>();
        defineAnonymousDocStringType(new DocStringType(Object.class, Object::toString));
    }


    public void defineDocStringType(DocStringType docStringType) {
        if (docStringType.getContentType().isEmpty()) {
            defineAnonymousDocStringType(docStringType);
        }
        else {
            defineNamedDocStringType(docStringType);
        }
    }

    private void defineAnonymousDocStringType(DocStringType docStringType) {
        DocStringType existing = anonymousDocstringTypes.get(docStringType.getTargetType());
        if (existing != null) {
            throw new CucumberException(format("There is already an anonymous docstring type registered for %s.", docStringType.getTargetType()));
        }
        anonymousDocstringTypes.put(docStringType.getTargetType(),docStringType);
    }

    private void defineNamedDocStringType(DocStringType docStringType) {
        DocStringType existing = namedDocstringTypes.get(docStringType.getContentType());
        if (existing!= null) {
           throw new CucumberException(format("" +
               "There is already docstring type registered for content type %s.\n" +
               "It registered as %s. You are trying to add a %s",
               existing.getContentType(), existing.getTargetType(), docStringType.getTargetType()));
       }
        namedDocstringTypes.put(docStringType.getContentType(), docStringType);
    }

    DocStringType lookUpByType(Type type) {
        DocStringType docStringType = anonymousDocstringTypes.get(type);
        if (docStringType == null) {
            throw new CucumberException(String.format("It appears you did not register docstring type for %s", type));
        }
        return anonymousDocstringTypes.get(type);
    }

    DocStringType lookUpByContentType(String contentType) {
        DocStringType docStringType = namedDocstringTypes.get(contentType);
        if (docStringType == null) {
            throw new CucumberException(String.format("It appears you did not register docstring type for content type %s", contentType));
        }
        return namedDocstringTypes.get(contentType);
    }

}

