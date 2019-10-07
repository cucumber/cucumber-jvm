package io.cucumber.java8;

import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.docstring.DocStringType;
import net.jodah.typetools.TypeResolver;

final class Java8DocStringTypeDefinition extends AbstractGlueDefinition implements DocStringTypeDefinition {

    private final DocStringType docStringType;

    @Override
    public DocStringType docStringType() {
        return docStringType;
    }

    Java8DocStringTypeDefinition(Object body, String contentType) {
        super(body, new Exception().getStackTrace()[3]);
        if (contentType == null) {
            throw new CucumberException("Docstring content type couldn't be null, define docstring content type");
        }
        if (contentType.isEmpty()) {
            throw new CucumberException("Docstring content type couldn't be empty, define docstring content type");
        }
        Class returnType = TypeResolver.resolveRawArguments(DocStringDefinitionBody.class, body.getClass())[0];
        this.docStringType = new DocStringType(
            returnType,
            contentType,
            this::execute);
    }

    private Object execute(String content) throws Throwable {
        return Invoker.invoke(this, body, method, content);
    }
}
