package io.cucumber.java8;

import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.docstring.DocStringType;

final class Java8DocStringTypeDefinition extends AbstractGlueDefinition implements DocStringTypeDefinition {

    private final DocStringType docStringType;

    Java8DocStringTypeDefinition(String contentType, DocStringDefinitionBody<?> body) {
        super(body, new Exception().getStackTrace()[3]);
        if (contentType == null) {
            throw new CucumberException("Docstring content type couldn't be null, define docstring content type");
        }
        if (contentType.isEmpty()) {
            throw new CucumberException("Docstring content type couldn't be empty, define docstring content type");
        }
        Class<?> returnType = resolveRawArguments(DocStringDefinitionBody.class, body.getClass())[0];
        this.docStringType = new DocStringType(
            returnType,
            contentType,
            this::invokeMethod);
    }

    @Override
    public DocStringType docStringType() {
        return docStringType;
    }

}
