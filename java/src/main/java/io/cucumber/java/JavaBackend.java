package io.cucumber.java;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.core.snippets.Snippet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import static java.lang.Thread.currentThread;

final class JavaBackend implements Backend {

    private final Lookup lookup;
    private final Container container;

    private final MethodScanner methodScanner;
    private Glue glue;

    JavaBackend(Lookup lookup, Container container, ResourceLoader resourceLoader) {
        this(lookup, container, new ResourceLoaderClassFinder(resourceLoader, currentThread().getContextClassLoader()));
    }

    JavaBackend(Lookup lookup, Container container, ClassFinder classFinder) {
        this.lookup = lookup;
        this.container = container;
        this.methodScanner = new MethodScanner(classFinder);
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        this.glue = glue;
        // Scan for Java7 style glue (annotated methods)
        methodScanner.scan(this, gluePaths);
    }

    @Override
    public void buildWorld() {

    }

    @Override
    public void disposeWorld() {

    }

    @Override
    public Snippet getSnippet() {
        return new JavaSnippet();
    }

    void addStepDefinition(Annotation annotation, Method method) {
        String expression = expression(annotation);
        long timeoutMillis = timeoutMillis(annotation);
        container.addClass(method.getDeclaringClass());
        glue.addStepDefinition(typeRegistry ->
            new JavaStepDefinition(method, expression, timeoutMillis, lookup, typeRegistry));
    }

    void addHook(Annotation annotation, Method method) {
        if (container.addClass(method.getDeclaringClass())) {
            if (annotation.annotationType().equals(Before.class)) {
                Before before = (Before) annotation;
                String tagExpression = before.value();
                long timeout = before.timeout();
                glue.addBeforeHook(new JavaHookDefinition(method, tagExpression, before.order(), timeout, lookup));
            } else if (annotation.annotationType().equals(After.class)) {
                After after = (After) annotation;
                String tagExpression = after.value();
                long timeout = after.timeout();
                glue.addAfterHook(new JavaHookDefinition(method, tagExpression, after.order(), timeout, lookup));
            } else if (annotation.annotationType().equals(BeforeStep.class)) {
                BeforeStep beforeStep = (BeforeStep) annotation;
                String tagExpression = beforeStep.value();
                long timeout = beforeStep.timeout();
                glue.addBeforeStepHook(new JavaHookDefinition(method, tagExpression, beforeStep.order(), timeout, lookup));
            } else if (annotation.annotationType().equals(AfterStep.class)) {
                AfterStep afterStep = (AfterStep) annotation;
                String tagExpression = afterStep.value();
                long timeout = afterStep.timeout();
                glue.addAfterStepHook(new JavaHookDefinition(method, tagExpression, afterStep.order(), timeout, lookup));
            }
        }
    }

    private String expression(Annotation annotation) {
        try {
            Method expressionMethod = annotation.getClass().getMethod("value");
            return (String) Invoker.invoke(annotation, expressionMethod, 0);
        } catch (Throwable e) {
            throw new CucumberException(e);
        }
    }

    private long timeoutMillis(Annotation annotation) {
        try {
            Method regexpMethod = annotation.getClass().getMethod("timeout");
            return (Long) Invoker.invoke(annotation, regexpMethod, 0);
        } catch (Throwable throwable) {
            throw new CucumberException(throwable);
        }
    }

}
