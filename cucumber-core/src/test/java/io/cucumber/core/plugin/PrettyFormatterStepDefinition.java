package io.cucumber.core.plugin;

import io.cucumber.core.backend.SourceReference;

import java.lang.reflect.Method;

class PrettyFormatterStepDefinition {
    SourceReference source;

    PrettyFormatterStepDefinition() {
        source = SourceReference.fromStackTraceElement(new Exception().getStackTrace()[0]);
    }

    public void one() {

    }

    public void two() {

    }
    public void three() {
        
    }

    static SourceReference oneReference() {
        return getSourceReference("one");
    }

    static SourceReference twoReference() {
        return getSourceReference("two");
    }

    static SourceReference threeReference() {
        return getSourceReference("three");
    }

    private static SourceReference getSourceReference(String methodName) {
        try {
            Method method = PrettyFormatterStepDefinition.class.getMethod(methodName);
            return SourceReference.fromMethod(method);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    static SourceReference getStackSourceReference() {
        return new PrettyFormatterStepDefinition().source;
    }
}
