package io.cucumber.java;

import io.cucumber.core.backend.CucumberBackendException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class InvalidMethodSignatureException extends CucumberBackendException {

    private InvalidMethodSignatureException(String message) {
        super(message);
    }

    static InvalidMethodSignatureExceptionBuilder builder(Method method) {
        return new InvalidMethodSignatureExceptionBuilder(method);
    }

    static class InvalidMethodSignatureExceptionBuilder {

        private final Method method;
        private final List<Class<?>> annotations = new ArrayList<>();
        private final List<String> signatures = new ArrayList<>();
        private final List<String> notes = new ArrayList<>();

        private InvalidMethodSignatureExceptionBuilder(Method method) {
            this.method = requireNonNull(method);
        }

        InvalidMethodSignatureExceptionBuilder addAnnotation(Class<?> annotation) {
            annotations.add(annotation);
            return this;
        }

        InvalidMethodSignatureExceptionBuilder addSignature(String signature) {
            signatures.add(signature);
            return this;
        }

        InvalidMethodSignatureExceptionBuilder addNote(String note) {
            this.notes.add(note);
            return this;
        }

        public InvalidMethodSignatureException build() {
            return new InvalidMethodSignatureException("" +
                    describeAnnotations() + " must have one of these signatures:\n" +
                    " * " + describeAvailableSignature() + "\n" +
                    "at " + describeLocation() + "\n" +
                    describeNote() + "\n");
        }

        private String describeAnnotations() {
            if (annotations.size() == 1) {
                return "A @" + annotations.get(0).getSimpleName() + " annotated method";
            }

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < annotations.size(); i++) {
                builder.append(annotations.get(i).getSimpleName());

                if (i < annotations.size() - 2) {
                    builder.append(", ");
                } else if (i < annotations.size() - 1) {
                    builder.append(" or ");
                }
            }

            return "A method annotated with " + builder.toString();
        }

        private String describeAvailableSignature() {
            return String.join("\n * ", signatures);
        }

        private Object describeLocation() {
            return MethodFormat.FULL.format(method);
        }

        private String describeNote() {
            return String.join("\n", notes);
        }

    }

}
