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

    public void oneArgument(String a) {

    }

    public void twoArguments(Integer a, Integer b) {

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

    static SourceReference twoArgumentsReference() {
        return getSourceReference("twoArguments", Integer.class, Integer.class);
    }

    static SourceReference oneArgumentsReference() {
        return getSourceReference("oneArgument", String.class);
    }

    private static SourceReference getSourceReference(String methodName, Class<?>... p) {
        try {
            Method method = PrettyFormatterStepDefinition.class.getMethod(methodName, p);
            return SourceReference.fromMethod(method);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    static SourceReference getStackSourceReference() {
        return new PrettyFormatterStepDefinition().source;
    }
}
