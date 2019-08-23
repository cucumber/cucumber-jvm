package io.cucumber.core.stepexpression;

import static java.lang.String.format;

import io.cucumber.core.exception.CucumberException;
import java.util.HashMap;
import java.util.Map;

public class DocStringTypeRegistry {

    private final Map<String, DocStringType> docStringTypes;

    public DocStringTypeRegistry() {
        docStringTypes = new HashMap<>();
        defineDocStringType(new DocStringType(String.class,"", (String docString) -> docString));
    }


    public void defineDocStringType(DocStringType docStringType) {
        DocStringType existing = docStringTypes.get(docStringType.getContentType());
        if (existing!= null) {
            throw new CucumberException(format("" +
                    "There is already docstring type registered for content type \"%s\".\n" +
                    "It registered as %s. You are trying to add a %s",
                existing.getContentType(), existing.getTargetType(), docStringType.getTargetType()));
        }
        docStringTypes.put(docStringType.getContentType(), docStringType);
    }

    DocStringType lookUpByContentType(String contentType) {
        return docStringTypes.get(contentType);
    }

}

