package cucumber.runtime.java;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.annotation.Order;
import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

public class ClasspathMethodScanner {

    private final ClasspathResourceLoader resourceLoader;

    public ClasspathMethodScanner(ClasspathResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void scan(JavaBackend javaBackend, List<String> gluePaths) {
        Collection<Class<? extends Annotation>> cucumberAnnotationClasses = findCucumberAnnotationClasses();
        for (String gluePath : gluePaths) {
            String packageName = gluePath.replace('/', '.').replace('\\', '.'); // Sometimes the gluePath will be a path, not a package
            for (Class<?> candidateClass : resourceLoader.getDescendants(Object.class, packageName)) {
                while (candidateClass != Object.class && !Utils.isInstantiable(candidateClass)) {
                    // those can't be instantiated without container class present.
                    candidateClass = candidateClass.getSuperclass();
                }
                for (Method method : candidateClass.getMethods()) {
                    scan(candidateClass, method, cucumberAnnotationClasses, javaBackend);
                }
            }
        }
    }

    private Collection<Class<? extends Annotation>> findCucumberAnnotationClasses() {
        return resourceLoader.getAnnotations("cucumber.annotation");
    }

    private void scan(Class<?> candidateClass, Method method, Collection<Class<? extends Annotation>> cucumberAnnotationClasses,
            JavaBackend javaBackend) {
        for (Class<? extends Annotation> cucumberAnnotationClass : cucumberAnnotationClasses) {
            Annotation annotation = method.getAnnotation(cucumberAnnotationClass);
            if (annotation != null && !annotation.annotationType().equals(Order.class)) {
                if (isHookAnnotation(annotation)) {
                    javaBackend.addHook(annotation, candidateClass, method);
                } else if (isStepdefAnnotation(annotation)) {
                    javaBackend.addStepDefinition(annotation, candidateClass, method);
                }
            }
        }
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
