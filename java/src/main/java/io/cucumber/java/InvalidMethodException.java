package io.cucumber.java;

import io.cucumber.core.exception.CucumberException;

import java.lang.reflect.Method;
import java.util.Objects;

final class InvalidMethodException extends CucumberException {

    private InvalidMethodException(String message) {
        super(message);
    }

    static InvalidMethodException createInvalidMethodException(Method method, Class<?> glueCodeClass) {
        if (Objects.isNull(method)) {
            throw new IllegalArgumentException("Supplied Method can't be null for InvalidMethodException");
        }
        return new InvalidMethodException(
            String.format("You're not allowed to extend classes that define Step Definitions or hooks. %s extends %s",
                glueCodeClass,
                method.getDeclaringClass()
            ));
    }

}
