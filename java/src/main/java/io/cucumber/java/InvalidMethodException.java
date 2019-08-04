package io.cucumber.java;

import io.cucumber.core.exception.CucumberException;

import java.lang.reflect.Method;

final class InvalidMethodException
    extends CucumberException {

    private InvalidMethodException(final String message) {
        super(message);
    }

    static InvalidMethodException createInvalidMethodException(final Method method, final Class<?> glueCodeClass) {
        return new InvalidMethodException(
            String.format(
                "You're not allowed to extend classes that define Step Definitions or hooks. %s extends %s",
                glueCodeClass,
                method.getDeclaringClass()
            ));
    }

}
