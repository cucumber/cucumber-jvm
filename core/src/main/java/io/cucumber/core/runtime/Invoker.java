package io.cucumber.core.runtime;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.reflection.MethodFormat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Invoker {

    private Invoker() {

    }

    static <T> T timeout(Callback<T> callback, long timeoutMillis) throws Throwable {
        if (timeoutMillis == 0) {
            return callback.call();
        }

        /* We need to ensure a happens before relation exists between these events;
         *   a. the timer setting the interrupt flag on the execution thread.
         *   b. terminating and cleaning up the timer
         * To do this we synchronize on monitor. The atomic boolean is merely a convenient container.
         */
        final Thread executionThread = Thread.currentThread();
        final Object monitor = new Object();
        final AtomicBoolean done = new AtomicBoolean();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> timer = executorService.schedule(() -> {
            synchronized (monitor) {
                if (!done.get()) {
                    executionThread.interrupt();
                }
            }
        }, timeoutMillis, TimeUnit.MILLISECONDS);

        try {
            T result = callback.call();
            // The callback may have been busy waiting.
            if (Thread.interrupted()) {
                throw new TimeoutException("Timed out after " + timeoutMillis + "ms.");
            }
            return result;
        } catch (InterruptedException timeout) {
            throw new TimeoutException("Timed out after " + timeoutMillis + "ms.");
        } finally {
            synchronized (monitor) {
                done.set(true);
                timer.cancel(true);
                executorService.shutdownNow();
                // Clear the interrupted flag. It may have been set by the timer just before we returned the result.
                Thread.interrupted();
            }
        }
    }

    /**
     * @deprecated timeout has been deprecated in favour of library solutions used by the end user.
     */
    @Deprecated
    public static Object invoke(Object target, Method method, long timeoutMillis, Object... args) throws Throwable {
        Method targetMethod = targetMethod(target, method);
        return timeout(() -> Invoker.invoke(target, targetMethod, args), timeoutMillis);
    }

    public static Object invoke(Object target, Method targetMethod, Object... args) throws Throwable {
        boolean accessible = targetMethod.isAccessible();
        try {
            targetMethod.setAccessible(true);
            return targetMethod.invoke(target, args);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new CucumberException("Failed to invoke " + MethodFormat.FULL.format(targetMethod) +
                ", caused by " + e.getClass().getName() + ": " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            targetMethod.setAccessible(accessible);
        }
    }

    private static Method targetMethod(Object target, Method method) throws NoSuchMethodException {
        Class<?> targetClass = target.getClass();
        Class<?> declaringClass = method.getDeclaringClass();

        // Immediately return the provided method if the class loaders are the same.
        if (targetClass.getClassLoader().equals(declaringClass.getClassLoader())) {
            return method;
        } else {
            // Check if the method is publicly accessible. Note that methods from interfaces are always public.
            if (Modifier.isPublic(method.getModifiers())) {
                return targetClass.getMethod(method.getName(), method.getParameterTypes());
            }

            // Loop through all the super classes until the declared method is found.
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
        }
    }

    interface Callback<T> {
        T call() throws Throwable;
    }
}
