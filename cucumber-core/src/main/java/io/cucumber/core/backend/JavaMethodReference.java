package io.cucumber.core.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class JavaMethodReference implements SourceReference {

    private final String className;
    private final String methodName;
    private final List<String> methodParameterTypes;

    JavaMethodReference(Class<?> declaringClass, String methodName, Class<?>[] methodParameterTypes) {
        this.className = requireNonNull(declaringClass).getName();
        this.methodName = requireNonNull(methodName);
        this.methodParameterTypes = new ArrayList<>(methodParameterTypes.length);
        for (Class<?> parameterType : methodParameterTypes) {
            this.methodParameterTypes.add(parameterType.getName());
        }
    }

    public String className() {
        return className;
    }

    public String methodName() {
        return methodName;
    }

    public List<String> methodParameterTypes() {
        return methodParameterTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JavaMethodReference that = (JavaMethodReference) o;
        return className.equals(that.className) &&
                methodName.equals(that.methodName) &&
                methodParameterTypes.equals(that.methodParameterTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, methodParameterTypes);
    }

}
