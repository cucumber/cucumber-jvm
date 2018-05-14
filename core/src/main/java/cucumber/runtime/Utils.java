package cucumber.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;

public class Utils {
    private Utils() {
    }

    public static boolean isInstantiable(Class<?> clazz) {
        boolean isNonStaticInnerClass = !Modifier.isStatic(clazz.getModifiers()) && clazz.getEnclosingClass() != null;
        return Modifier.isPublic(clazz.getModifiers()) && !Modifier.isAbstract(clazz.getModifiers()) && !isNonStaticInnerClass;
    }

    public static Object invoke(final Object target, final Method method, long timeoutMillis, final Object... args) throws Throwable {
        final Method targetMethod = targetMethod(target, method);
        return Timeout.timeout(new Timeout.Callback<Object>() {
            @Override
            public Object call() throws Throwable {
                boolean accessible = targetMethod.isAccessible();
                try {
                    targetMethod.setAccessible(true);
                    return targetMethod.invoke(target, args);
                } catch (IllegalArgumentException e) {
                    throw new CucumberException("Failed to invoke " + MethodFormat.FULL.format(targetMethod) +
                                                ", caused by " + e.getClass().getName() + ": " + e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                } catch (IllegalAccessException e) {
                    throw new CucumberException("Failed to invoke " + MethodFormat.FULL.format(targetMethod) +
                                                ", caused by " + e.getClass().getName() + ": " + e.getMessage(), e);
                } finally {
                    targetMethod.setAccessible(accessible);
                }
            }
        }, timeoutMillis);
    }

    private static Method targetMethod(final Object target, final Method method) throws NoSuchMethodException {
        final Class<?> targetClass = target.getClass();
        final Class<?> declaringClass = method.getDeclaringClass();

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

    public static URL toURL(String pathOrUrl) {
        try {
            if (!pathOrUrl.endsWith("/")) {
                pathOrUrl = pathOrUrl + "/";
            }
            if (pathOrUrl.matches("^(file|http|https):.*")) {
                return new URL(pathOrUrl);
            } else {
                return new URL("file:" + pathOrUrl);
            }
        } catch (MalformedURLException e) {
            throw new CucumberException("Bad URL:" + pathOrUrl, e);
        }
    }

    public static String htmlEscape(String s) {
        // https://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention_Cheat_Sheet
        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    public static String getUniqueTestNameForScenarioExample(String testCaseName, int exampleNumber) {
        return testCaseName + (includesBlank(testCaseName) ? " " : "_") + exampleNumber;
    }

    private static boolean includesBlank(String testCaseName) {
        return testCaseName.indexOf(' ') != -1;
    }

}