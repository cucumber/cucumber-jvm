package cucumber.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Utils {
    public static <T> List<T> listOf(int size, T obj) {
        List<T> list = new ArrayList<T>();
        for (int i = 0; i < size; i++) {
            list.add(obj);
        }
        return list;
    }

    public static boolean isInstantiable(Class<?> clazz) {
        boolean isNonStaticInnerClass = !Modifier.isStatic(clazz.getModifiers()) && clazz.getEnclosingClass() != null;
        return Modifier.isPublic(clazz.getModifiers()) && !Modifier.isAbstract(clazz.getModifiers()) && !isNonStaticInnerClass;
    }

    public static boolean hasConstructor(Class<?> clazz, Class[] paramTypes) {
        try {
            clazz.getConstructor(paramTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static Object invoke(final Object target, final Method method, int timeoutMillis, final Object... args) throws Throwable {
        try {
            if (timeoutMillis == 0) {
                return method.invoke(target, args);
            } else {
                final Thread executionThread = Thread.currentThread();
                final AtomicBoolean done = new AtomicBoolean();
                Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (!done.get()) {
                            executionThread.interrupt();
                        }
                    }
                }, timeoutMillis, TimeUnit.MILLISECONDS);
                try {
                    return invoke(target, method, 0, args);
                } catch (InterruptedException timeout) {
                    throw new TimeoutException("Timed out after " + timeoutMillis + "ms.");
                }
            }
        } catch (IllegalArgumentException e) {
            throw new CucumberException("Failed to invoke " + MethodFormat.FULL.format(method), e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (IllegalAccessException e) {
            throw new CucumberException("Failed to invoke " + MethodFormat.FULL.format(method), e);
        }
    }
}