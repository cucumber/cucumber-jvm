package io.cucumber.java;

import io.cucumber.core.exception.CucumberException;

import java.lang.reflect.Method;

final class InvalidMethodException extends CucumberException {

    private InvalidMethodException(String message) {
        super(message);
    }

    static InvalidMethodException createInvalidMethodException(Method method, Class<?> glueCodeClass) {
        return  new InvalidMethodException(
            "You're not allowed to extend classes that define Step Definitions or hooks. "
                + glueCodeClass + " extends " + method.getDeclaringClass());
    }

    static InvalidMethodException createMethodDeclaringClassNotAssignableFromGlue(Method method, Class<?> glueCodeClass) {
        return new InvalidMethodException(method.getDeclaringClass() + " isn't assignable from " + glueCodeClass);
    }

}
