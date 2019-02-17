package io.cucumber.java;

import gherkin.pickles.PickleStep;
import io.cucumber.core.api.options.SnippetType;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.core.snippets.SnippetGenerator;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.java.api.After;
import io.cucumber.java.api.AfterStep;
import io.cucumber.java.api.Before;
import io.cucumber.java.api.BeforeStep;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import static java.lang.Thread.currentThread;

public class JavaBackend implements Backend {

    private final TypeRegistry typeRegistry;
    private final SnippetGenerator annotationSnippetGenerator;

    private final ObjectFactory objectFactory;

    private final MethodScanner methodScanner;
    private Glue glue;

    JavaBackend(ObjectFactory objectFactory, ResourceLoader resourceLoader, TypeRegistry typeRegistry) {
        this(objectFactory, new ResourceLoaderClassFinder(resourceLoader, currentThread().getContextClassLoader()), typeRegistry);
    }

    JavaBackend(ObjectFactory objectFactory, ClassFinder classFinder, TypeRegistry typeRegistry) {
        this.objectFactory = objectFactory;
        this.methodScanner = new MethodScanner(classFinder);
        this.annotationSnippetGenerator = new SnippetGenerator(new JavaSnippet(), typeRegistry.parameterTypeRegistry());
        this.typeRegistry = typeRegistry;
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        this.glue = glue;
        // Scan for Java7 style glue (annotated methods)
        methodScanner.scan(this, gluePaths);
    }

    @Override
    public void buildWorld() {
        objectFactory.start();
    }

    @Override
    public void disposeWorld() {
        objectFactory.stop();
    }

    @Override
    public List<String> getSnippet(PickleStep step, String keyword, SnippetType.FunctionNameGenerator functionNameGenerator) {
        return annotationSnippetGenerator.getSnippet(step, keyword, functionNameGenerator);
    }

    void addStepDefinition(Annotation annotation, Method method) {
        try {
            objectFactory.addClass(method.getDeclaringClass());
            glue.addStepDefinition(
                new JavaStepDefinition(
                    method,
                    expression(annotation),
                    timeoutMillis(annotation),
                    objectFactory,
                    typeRegistry));
        } catch (CucumberException e) {
            throw e;
        } catch (Throwable e) {
            throw new CucumberException(e);
        }
    }

    void addHook(Annotation annotation, Method method) {
        if (objectFactory.addClass(method.getDeclaringClass())) {
            if (annotation.annotationType().equals(Before.class)) {
                String tagExpression = ((Before) annotation).value();
                long timeout = ((Before) annotation).timeout();
                glue.addBeforeHook(new JavaHookDefinition(method, tagExpression, ((Before) annotation).order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(After.class)) {
                String tagExpression = ((After) annotation).value();
                long timeout = ((After) annotation).timeout();
                glue.addAfterHook(new JavaHookDefinition(method, tagExpression, ((After) annotation).order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(BeforeStep.class)) {
                String tagExpression = ((BeforeStep) annotation).value();
                long timeout = ((BeforeStep) annotation).timeout();
                glue.addBeforeStepHook(new JavaHookDefinition(method, tagExpression, ((BeforeStep) annotation).order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(AfterStep.class)) {
                String tagExpression = ((AfterStep) annotation).value();
                long timeout = ((AfterStep) annotation).timeout();
                glue.addAfterStepHook(new JavaHookDefinition(method, tagExpression, ((AfterStep) annotation).order(), timeout, objectFactory));
            }
        }
    }

    private String expression(Annotation annotation) throws Throwable {
        Method expressionMethod = annotation.getClass().getMethod("value");
        return (String) Invoker.invoke(annotation, expressionMethod, 0);
    }

    private long timeoutMillis(Annotation annotation) throws Throwable {
        Method regexpMethod = annotation.getClass().getMethod("timeout");
        return (Long) Invoker.invoke(annotation, regexpMethod, 0);
    }

}
