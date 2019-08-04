package io.cucumber.java;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.reflection.MethodFormat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class InvalidMethodSignatureException extends CucumberException {

    private InvalidMethodSignatureException(String message) {
        super(message);
    }

    static InvalidMethodSignatureExceptionBuilder builder(Method method) {
        if (Objects.isNull(method)) {
            throw new IllegalArgumentException("Supplied Method can't be null for InvalidMethodSignatureException");
        }
        return new InvalidMethodSignatureExceptionBuilder(method);
    }

    static class InvalidMethodSignatureExceptionBuilder {

        private Method method;
        private final List<Class<?>> annotations = new ArrayList<>();
        private final List<String> signatures = new ArrayList<>();
        private final List<String> notes = new ArrayList<>();

        private InvalidMethodSignatureExceptionBuilder(Method method) {
            this.method = method;
        }

        InvalidMethodSignatureExceptionBuilder addAnnotation(Class<?> annotation) {
            this.annotations.add(annotation);
            return this;
        }

        InvalidMethodSignatureExceptionBuilder addSignature(String signature) {
            this.signatures.add(signature);
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
                describeNote() + "\n"
            );
        }

        private String describeNote() {
            return String.join("\n", this.notes);
        }

        private Object describeLocation() {
            return MethodFormat.FULL.format(this.method);
        }

        private String describeAvailableSignature() {
            return String.join("\n * ", this.signatures);
        }

        private String describeAnnotations() {
            if (this.annotations.size() == 1) {
                return "A @" + this.annotations.get(0).getSimpleName() + " annotated method";
            }

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < this.annotations.size(); i++) {
                builder.append(this.annotations.get(i).getSimpleName());

                if (i < this.annotations.size() - 2) {
                    builder.append(", ");
                } else if (i < this.annotations.size() - 1) {
                    builder.append(" or ");
                }
            }

            return "A method annotated with " + builder.toString();
        }
    }

}
