package io.cucumber.core.backend;

import java.lang.reflect.Method;
import java.util.Optional;

public class StubLocation implements Located {

    private final String location;
    private final SourceReference sourceReference;

    public StubLocation(String location) {
        this.location = location;
        this.sourceReference = null;
    }

    public StubLocation(Method method) {
        this.location = null;
        this.sourceReference = SourceReference.fromMethod(method);
    }

    public StubLocation(SourceReference sourceReference) {
        this.sourceReference = sourceReference;
        this.location = formatLocation(sourceReference);
    }

    private static String formatLocation(SourceReference sourceReference) {
        if (sourceReference instanceof JavaMethodReference) {
            JavaMethodReference javaMethodReference = (JavaMethodReference) sourceReference;
            String className = javaMethodReference.className();
            String methodName = javaMethodReference.methodName();
            String parameterTypes = String.join(",", javaMethodReference.methodParameterTypes());
            return String.format("%s#%s(%s)", className, methodName, parameterTypes);
        }
        return null;
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
