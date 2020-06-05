package io.cucumber.java;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.Located;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class Invoker {

    private Invoker() {

    }

    static Object invoke(Annotation annotation, Method expressionMethod) {
        return invoke(null, annotation, expressionMethod);
    }

    static Object invoke(Located located, Object target, Method method, Object... args) {
        Method targetMethod = targetMethod(target, method);
        return doInvoke(located, target, targetMethod, args);
    }

    private static Method targetMethod(Object target, Method method) {
        Class<?> targetClass = target.getClass();
        Class<?> declaringClass = method.getDeclaringClass();

        // Immediately return the provided method if the class loaders are the
        // same.
        if (targetClass.getClassLoader().equals(declaringClass.getClassLoader())) {
            return method;
        }

        try {
            // Check if the method is publicly accessible. Note that methods
            // from interfaces are always public.
            if (Modifier.isPublic(method.getModifiers())) {
                return targetClass.getMethod(method.getName(), method.getParameterTypes());
            }

            // Loop through all the super classes until the declared method is
            // found.
            Class<?> currentClass = targetClass;
            while (currentClass != Object.class) {
                try {
                    return currentClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException e) {
                    currentClass = currentClass.getSuperclass();
                }
            }

            // The method does not exist in the class hierarchy.
            throw new NoSuchMethodException(String.valueOf(method));
        } catch (NoSuchMethodException e) {
            throw new CucumberBackendException("Could not find target method", e);
        }
    }

    private static Object doInvoke(Located located, Object target, Method targetMethod, Object[] args) {
        boolean accessible = targetMethod.isAccessible();
        try {
            targetMethod.setAccessible(true);
            return targetMethod.invoke(target, args);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new CucumberBackendException("Failed to invoke " + targetMethod, e);
        } catch (InvocationTargetException e) {
            if (located == null) { // Reflecting into annotations
                throw new CucumberBackendException("Failed to invoke " + targetMethod, e);
            }
            throw new CucumberInvocationTargetException(located, e);
        } finally {
            targetMethod.setAccessible(accessible);
        }
    }

    static Object invokeStatic(Located located, Method method, Object... args) {
        return doInvoke(located, null, method, args);
    }

}
