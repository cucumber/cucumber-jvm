package cucumber.runtime.java;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.classpath.Classpath;
import cuke4duke.internal.Utils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

public class ClasspathMethodScanner {
    public void scan(JavaBackend javaBackend, String packagePrefix) {
        try {
            Collection<Class<? extends Annotation>> cucumberAnnotations = findCucumberAnnotationClasses();
            for (Class<?> clazz : Classpath.getInstantiableClasses(packagePrefix)) {
                try {
                    if(Modifier.isPublic(clazz.getModifiers()) && !Modifier.isAbstract(clazz.getModifiers())) {
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
        return Classpath.getInstantiableSubclassesOf(Annotation.class, "cucumber.annotation");
    }

    private void scan(Method method, Collection<Class<? extends Annotation>> cucumberAnnotationClasses, JavaBackend javaBackend) {
        for (Class<? extends Annotation> cucumberAnnotationClass : cucumberAnnotationClasses) {
            Annotation annotation = method.getAnnotation(cucumberAnnotationClass);
            if (annotation != null) {
                if (isHookAnnotation(annotation)) {
                    // TODO Add hook
                }
                //TODO: scan cucumber.annotation.Transform annotations
                Locale locale = Utils.localeFor(annotation.annotationType().getAnnotation(CucumberAnnotation.class).value());
                try {
                    Method regexpMethod = annotation.getClass().getMethod("value");
                    String regexpString = (String) regexpMethod.invoke(annotation);
                    if (regexpString != null) {
                        Pattern pattern = Pattern.compile(regexpString);
                        javaBackend.addStepDefinition(pattern, method, locale);
                    }
                } catch (NoSuchMethodException ignore) {
                } catch (IllegalAccessException ignore) {
                } catch (InvocationTargetException ignore) {
                }
            }
        }
    }

    private boolean isHookAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.getClass();
        return annotationClass.equals(Before.class) || annotationClass.equals(After.class);
    }
}
