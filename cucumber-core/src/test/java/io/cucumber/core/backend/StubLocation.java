package io.cucumber.core.backend;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class StubLocation implements Located {

    private final String location;
    private final @Nullable SourceReference sourceReference;

    public StubLocation(String location) {
        this.location = location;
        this.sourceReference = null;
    }

    public StubLocation(SourceReference sourceReference) {
        this.sourceReference = sourceReference;
        this.location = formatLocation(sourceReference);
    }

    private static String formatLocation(SourceReference sourceReference) {
        if (sourceReference instanceof JavaMethodReference javaMethodReference) {
            String className = javaMethodReference.className();
            String methodName = javaMethodReference.methodName();
            String parameterTypes = String.join(",", javaMethodReference.methodParameterTypes());
            return "%s#%s(%s)".formatted(className, methodName, parameterTypes);
        }
        throw new IllegalArgumentException(sourceReference.toString());
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return false;
    }

    @Override
    public Optional<SourceReference> getSourceReference() {
        return Optional.ofNullable(sourceReference);
    }

    @Override
    public String getLocation() {
        return location;
    }

}
