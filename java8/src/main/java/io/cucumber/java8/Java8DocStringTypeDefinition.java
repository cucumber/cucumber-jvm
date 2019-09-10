package io.cucumber.java8;

import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.docstring.DocStringType;

public class Java8DocStringTypeDefinition extends AbstractGlueDefinition implements DocStringTypeDefinition {

    private final DocStringType docStringType;

    @Override
    public DocStringType docStringType() {
        return docStringType;
    }

    Java8DocStringTypeDefinition(Object body, String contentType) {
        super(body, new Exception().getStackTrace()[3]);
        this.docStringType = new DocStringType(
            this.method.getReturnType(),
            contentType.isEmpty() ? method.getName() : contentType,
            this::execute);
    }

    private Object execute(String content) throws Throwable {
        return Invoker.invoke(body, method, content);
    }
}
