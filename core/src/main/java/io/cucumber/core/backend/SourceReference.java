package io.cucumber.core.backend;

import java.lang.reflect.Method;

public interface SourceReference {

    static SourceReference fromMethod(Method method) {
        return new JavaMethodReference(
            method.getDeclaringClass(),
            method.getName(),
            method.getParameterTypes());
    }

    static SourceReference fromStackTraceElement(StackTraceElement stackTraceElement) {
        return new StackTraceElementReference(
            stackTraceElement.getClassName(),
            stackTraceElement.getMethodName(),
            stackTraceElement.getFileName(),
            stackTraceElement.getLineNumber());
    }

}
