package cucumber.runtime.java;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.runtime.Reflections;
import cuke4duke.internal.Utils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class ClasspathMethodScanner implements MethodScanner {
    public void scan(JavaMethodBackend javaMethodBackend, String packagePrefix) {
        try {
            Set<Class<? extends Annotation>> cucumberAnnotations = findCucumberAnnotationClasses();
            for (Class<?> clazz : Reflections.getClasses(packagePrefix)) {
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (Reflections.isPublic(method.getModifiers())) {
                        scan(method, cucumberAnnotations, javaMethodBackend);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Class<? extends Annotation>> findCucumberAnnotationClasses() throws IOException {
        return Reflections.getSubtypesOf(Annotation.class, "cucumber.annotation");
    }

    private void scan(Method method, Set<Class<? extends Annotation>> cucumberAnnotationClasses, JavaMethodBackend javaMethodBackend) {
        for (Class<? extends Annotation> cucumberAnnotationClass : cucumberAnnotationClasses) {
            Annotation annotation = method.getAnnotation(cucumberAnnotationClass);
            if (annotation != null) {
                if (isHookAnnotation(annotation)) {
                    // TODO Add hook
                }

                Locale locale = Utils.localeFor(annotation.annotationType().getAnnotation(CucumberAnnotation.class).value());
                try {
                    Method regexpMethod = annotation.getClass().getMethod("value");
                    String regexpString = (String) regexpMethod.invoke(annotation);
                    if (regexpString != null) {
                        Pattern pattern = Pattern.compile(regexpString);
                        javaMethodBackend.addStepDefinition(pattern, method, locale);
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
