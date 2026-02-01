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

    static @Nullable Object invoke(Located located, Object target, Method method, @Nullable Object... args) {
        boolean accessible = method.canAccess(target);
        try {
            if (!accessible) {
                method.setAccessible(true);
            }
            return method.invoke(target, args);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new CucumberBackendException("Failed to invoke " + method, e);
        } catch (InvocationTargetException e) {
            throw new CucumberInvocationTargetException(located, e);
        } finally {
            if (!accessible) {
                method.setAccessible(false);
            }
        }
    }

}
