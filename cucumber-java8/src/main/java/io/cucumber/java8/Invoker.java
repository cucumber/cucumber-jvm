package io.cucumber.java8;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.Located;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class Invoker {

    private Invoker() {

    }

    @SuppressWarnings("deprecation") // isAccessible is deprecated in Java 9,
                                     // but canAccess not available on Android
    static @Nullable Object invoke(Located located, Object target, Method method, @Nullable Object... args) {
        try {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method.invoke(target, args);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new CucumberBackendException("Failed to invoke " + method, e);
        } catch (InvocationTargetException e) {
            throw new CucumberInvocationTargetException(located, e);
        }
    }

}
