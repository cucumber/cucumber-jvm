package io.cucumber.java;

import io.cucumber.core.backend.CucumberBackendException;

import java.lang.reflect.Method;

final class InvalidMethodException extends CucumberBackendException {

    private InvalidMethodException(String message) {
        super(message);
    }

    static InvalidMethodException annotatedMethodInParentClass(Method method, Class<?> glueCodeClass) {
        return new InvalidMethodException("""
                "%s" extends "%s" which declares a step definition or hook "%s".

                It is not possible to extend classes that define step definitions or hooks.

                If you are trying to share state between steps consider using dependency injection such as:
                     * cucumber-picocontainer
                     * cucumber-spring
                     * cucumber-jakarta-cdi
                     * ...etc
                """.formatted(glueCodeClass.getName(), method.getDeclaringClass().getName(),
            MethodFormat.FULL.format(method)));
    }

    static InvalidMethodException invalidModifier(Method method) {
        return new InvalidMethodException("""
                "%s" is not valid step definition.

                Step definitions and hooks must be either public, protected or package-private.
                They may not be private or abstract.
                """.formatted(MethodFormat.FULL.format(method)));
    }

}
