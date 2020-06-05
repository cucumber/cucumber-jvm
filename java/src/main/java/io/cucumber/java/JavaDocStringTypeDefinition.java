package io.cucumber.java;

import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.docstring.DocStringType;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static io.cucumber.java.InvalidMethodSignatureException.builder;

class JavaDocStringTypeDefinition extends AbstractGlueDefinition implements DocStringTypeDefinition {

    private final io.cucumber.docstring.DocStringType docStringType;

    JavaDocStringTypeDefinition(String contentType, Method method, Lookup lookup) {
        super(requireValidMethod(method), lookup);
        this.docStringType = new DocStringType(
            this.method.getReturnType(),
            contentType.isEmpty() ? method.getName() : contentType,
            this::invokeMethod);
    }

    private static Method requireValidMethod(Method method) {
        Type returnType = method.getGenericReturnType();
        if (Void.class.equals(returnType) || void.class.equals(returnType)) {
            throw createInvalidSignatureException(method);
        }

        Type[] parameterTypes = method.getGenericParameterTypes();
        if (parameterTypes.length != 1) {
            throw createInvalidSignatureException(method);
        }

        for (Type parameterType : parameterTypes) {
            if (!String.class.equals(parameterType)) {
                throw createInvalidSignatureException(method);
            }
        }

        return method;
    }

    private static InvalidMethodSignatureException createInvalidSignatureException(Method method) {
        return builder(method)
                .addAnnotation(io.cucumber.java.DocStringType.class)
                .addSignature("public JsonNode json(String content)")
                .addNote("Note: JsonNode is an example of the class you want to convert content to")
                .build();
    }

    @Override
    public DocStringType docStringType() {
        return docStringType;
    }

}
