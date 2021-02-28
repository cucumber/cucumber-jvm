package io.cucumber.core.backend;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class StackTraceElementReference implements SourceReference {

    private final String className;
    private final String methodName;
    private final String fileName;
    private final int lineNumber;

    StackTraceElementReference(String className, String methodName, String fileName, int lineNumber) {
        this.className = requireNonNull(className);
        this.methodName = requireNonNull(methodName);
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    public String className() {
        return className;
    }

    public String methodName() {
        return methodName;
    }

    public Optional<String> fileName() {
        return Optional.ofNullable(fileName);
    }

    public int lineNumber() {
        return lineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StackTraceElementReference that = (StackTraceElementReference) o;
        return lineNumber == that.lineNumber &&
                className.equals(that.className) &&
                methodName.equals(that.methodName) &&
                Objects.equals(fileName, that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, fileName, lineNumber);
    }

}
