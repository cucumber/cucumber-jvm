package io.cucumber.junit;

import io.cucumber.core.exception.CucumberException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

final class Assertions {

    private Assertions() {
    }

    static void assertNoCucumberAnnotatedMethods(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType().getName().startsWith("io.cucumber")) {
                    throw new CucumberException("\n\n" +
                            "Classes annotated with @RunWith(Cucumber.class) must not define any\n" +
                            "Step Definition or Hook methods. Their sole purpose is to serve as\n" +
                            "an entry point for JUnit. Step Definitions and Hooks should be defined\n" +
                            "in their own classes. This allows them to be reused across features.\n" +
                            "Offending class: " + clazz + "\n");
                }
            }
        }
    }

}
