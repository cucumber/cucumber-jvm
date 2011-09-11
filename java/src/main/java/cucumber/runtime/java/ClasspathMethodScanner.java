package cucumber.runtime.java;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.annotation.Order;
import cucumber.resources.Resources;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.regex.Pattern;

public class ClasspathMethodScanner {
    public void scan(JavaBackend javaBackend, String packagePrefix) {
        try {
            Collection<Class<? extends Annotation>> cucumberAnnotations = findCucumberAnnotationClasses();
            for (Class<?> clazz : Resources.getInstantiableClasses(packagePrefix)) {
                try {
                    if (Modifier.isPublic(clazz.getModifiers()) && !Modifier.isAbstract(clazz.getModifiers())) {
                        // TODO: How do we know what other dependendencies to add?
                    }
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        if (Modifier.isPublic(method.getModifiers())) {
                            scan(method, cucumberAnnotations, javaBackend);
                        }
                    }
                } catch (NoClassDefFoundError ignore) {
                } catch (SecurityException ignore) {
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<Class<? extends Annotation>> findCucumberAnnotationClasses() throws IOException {
        return Resources.getInstantiableSubclassesOf(Annotation.class, "cucumber.annotation");
    }

    private void scan(Method method, Collection<Class<? extends Annotation>> cucumberAnnotationClasses, JavaBackend javaBackend) {
        for (Class<? extends Annotation> cucumberAnnotationClass : cucumberAnnotationClasses) {
            Annotation annotation = method.getAnnotation(cucumberAnnotationClass);
            if (annotation != null && !annotation.annotationType().equals(Order.class)) {
                if (isHookAnnotation(annotation)) {
                    javaBackend.registerHook(annotation, method);
                } else {
                    try {
                        Method regexpMethod = annotation.getClass().getMethod("value");
                        String regexpString = (String) regexpMethod.invoke(annotation);
                        if (regexpString != null) {
                            Pattern pattern = Pattern.compile(regexpString);
                            javaBackend.addStepDefinition(pattern, method);
                        }
                    } catch (NoSuchMethodException ignore) {
                    } catch (IllegalAccessException ignore) {
                    } catch (InvocationTargetException ignore) {
                    }
                }
            }
        }
    }

    private boolean isHookAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();
        return annotationClass.equals(Before.class) || annotationClass.equals(After.class);
    }
}
