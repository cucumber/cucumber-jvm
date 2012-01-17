package cucumber.runtime.java;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.annotation.Order;
import cucumber.io.ClasspathResourceLoader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClasspathMethodScanner {

    private ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader();
    //Blargh, because there's parts used in the ClassPathResourceLoader, I cannot simply use the OneTimeResourceLoader
    private Set<String> loadedResourcePaths = new HashSet<String>();


    public void scan(JavaBackend javaBackend, List<String> gluePaths) {
        Collection<Class<? extends Annotation>> cucumberAnnotationClasses = findCucumberAnnotationClasses();
        for (String gluePath : gluePaths) {
            if (loadedResourcePaths.add(gluePath)) {
                for (Class<?> candidateClass : resourceLoader.getDescendants(Object.class, gluePath)) {
                    for (Method method : candidateClass.getMethods()) {
                        scan(method, cucumberAnnotationClasses, javaBackend);
                    }
                }
            }
        }
    }

    private Collection<Class<? extends Annotation>> findCucumberAnnotationClasses() {
        return resourceLoader.getAnnotations("cucumber/annotation");
    }

    private void scan(Method method, Collection<Class<? extends Annotation>> cucumberAnnotationClasses, JavaBackend javaBackend) {
        for (Class<? extends Annotation> cucumberAnnotationClass : cucumberAnnotationClasses) {
            Annotation annotation = method.getAnnotation(cucumberAnnotationClass);
            if (annotation != null && !annotation.annotationType().equals(Order.class)) {
                if (isHookAnnotation(annotation)) {
                    javaBackend.addHook(annotation, method);
                } else if (isStepdefAnnotation(annotation)) {
                    javaBackend.addStepDefinition(annotation, method);
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
