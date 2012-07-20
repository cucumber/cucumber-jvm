package cucumber.runtime.java;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.annotation.Order;
import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static cucumber.io.MultiLoader.packageName;

class ClasspathMethodScanner {

    private final ClasspathResourceLoader resourceLoader;
    private final Collection<Class<? extends Annotation>> cucumberAnnotationClasses;

    public ClasspathMethodScanner(ClasspathResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        cucumberAnnotationClasses = findCucumberAnnotationClasses();
    }

    /**
     * Registers step definitions and hooks.
     *
     * @param javaBackend the backend where stepdefs and hooks will be registered
     * @param gluePaths   where to look
     */
    public void scan(JavaBackend javaBackend, List<String> gluePaths) {
        for (String gluePath : gluePaths) {
            for (Class<?> glueCodeClass : resourceLoader.getDescendants(Object.class, packageName(gluePath))) {
                while (glueCodeClass != null && glueCodeClass != Object.class && !Utils.isInstantiable(glueCodeClass)) {
                    // those can't be instantiated without container class present.
                    glueCodeClass = glueCodeClass.getSuperclass();
                }
                if (glueCodeClass != null) {
                    for (Method method : glueCodeClass.getMethods()) {
                        scan(javaBackend, method, glueCodeClass);
                    }
                }
            }
        }
    }

    /**
     * Registers step definitions and hooks.
     *
     * @param javaBackend   the backend where stepdefs and hooks will be registered.
     * @param method        a candidate for being a stepdef or hook.
     * @param glueCodeClass the class where the method is declared.
     */
    public void scan(JavaBackend javaBackend, Method method, Class<?> glueCodeClass) {
        for (Class<? extends Annotation> cucumberAnnotationClass : cucumberAnnotationClasses) {
            Annotation annotation = method.getAnnotation(cucumberAnnotationClass);
            if (annotation != null && !annotation.annotationType().equals(Order.class)) {
                if (!method.getDeclaringClass().isAssignableFrom(glueCodeClass)) {
                    throw new CucumberException(String.format("%s isn't assignable from %s", method.getDeclaringClass(), glueCodeClass));
                }
                if (!glueCodeClass.equals(method.getDeclaringClass())) {
                    throw new CucumberException(String.format("You're not allowed to extend classes that define Step Definitions or hooks. %s extends %s", glueCodeClass, method.getDeclaringClass()));
                }
                if (isHookAnnotation(annotation)) {
                    javaBackend.addHook(annotation, method);
                } else if (isStepdefAnnotation(annotation)) {
                    javaBackend.addStepDefinition(annotation, method);
                }
            }
        }
    }

    private Collection<Class<? extends Annotation>> findCucumberAnnotationClasses() {
        return resourceLoader.getAnnotations("cucumber.annotation");
    }

    private boolean isHookAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();
        return annotationClass.equals(Before.class) || annotationClass.equals(After.class);
    }

    private boolean isStepdefAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();
        return annotationClass.getAnnotation(StepDefAnnotation.class) != null;
    }
}
