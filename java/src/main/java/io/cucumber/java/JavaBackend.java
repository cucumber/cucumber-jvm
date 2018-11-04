package io.cucumber.java;

import gherkin.pickles.PickleStep;
import io.cucumber.core.api.options.SnippetType;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.options.Env;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.core.snippets.SnippetGenerator;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.java.api.After;
import io.cucumber.java.api.AfterStep;
import io.cucumber.java.api.Before;
import io.cucumber.java.api.BeforeStep;
import io.cucumber.java.api.ObjectFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static io.cucumber.core.io.MultiLoader.packageName;
import static io.cucumber.java.ObjectFactoryLoader.loadObjectFactory;
import static java.lang.Thread.currentThread;

public class JavaBackend implements Backend, LambdaGlueRegistry {

    private final TypeRegistry typeRegistry;
    private final SnippetGenerator annotationSnippetGenerator;
    private final SnippetGenerator lambdaSnippetGenerator;

    private final ObjectFactory objectFactory;
    private final ClassFinder classFinder;

    private final MethodScanner methodScanner;
    private Glue glue;
    private List<Class<? extends LambdaGlue>> lambdaGlueClasses = new ArrayList<>();

    /**
     * The constructor called by reflection by default.
     *
     * @param resourceLoader
     */
    public JavaBackend(ResourceLoader resourceLoader, TypeRegistry typeRegistry) {
        this(new ResourceLoaderClassFinder(resourceLoader, currentThread().getContextClassLoader()), typeRegistry);
    }

    private JavaBackend(ClassFinder classFinder, TypeRegistry typeRegistry) {
        this(loadObjectFactory(Env.INSTANCE.get(ObjectFactory.class.getName())), classFinder, typeRegistry);
    }

    public JavaBackend(ObjectFactory objectFactory, ClassFinder classFinder, TypeRegistry typeRegistry) {
        this.classFinder = classFinder;
        this.objectFactory = objectFactory;
        this.methodScanner = new MethodScanner(classFinder);
        this.annotationSnippetGenerator = new SnippetGenerator(new JavaSnippet(), typeRegistry.parameterTypeRegistry());
        this.lambdaSnippetGenerator = new SnippetGenerator(new Java8Snippet(), typeRegistry.parameterTypeRegistry());
        this.typeRegistry = typeRegistry;
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        this.glue = glue;
        // Scan for Java7 style glue (annotated methods)
        methodScanner.scan(this, gluePaths);

        // Scan for Java8 style glue (lambdas)
        for (final String gluePath : gluePaths) {
            Collection<Class<? extends LambdaGlue>> glueDefinerClasses = classFinder.getDescendants(LambdaGlue.class, packageName(gluePath));
            for (final Class<? extends LambdaGlue> glueClass : glueDefinerClasses) {
                if (glueClass.isInterface()) {
                    continue;
                }

                if (objectFactory.addClass(glueClass)) {
                    lambdaGlueClasses.add(glueClass);
                }
            }
        }
    }

    /**
     * Convenience method for frameworks that wish to load glue from methods explicitly (possibly
     * found with a different mechanism than Cucumber's built-in classpath scanning).
     *
     * @param glue          where stepdefs and hooks will be added.
     * @param method        a candidate method.
     * @param glueCodeClass the class implementing the method. Must not be a subclass of the class implementing the method.
     */
    public void loadGlue(Glue glue, Method method, Class<?> glueCodeClass) {
        this.glue = glue;
        methodScanner.scan(this, method, glueCodeClass);
    }

    @Override
    public void buildWorld() {
        objectFactory.start();

        // Instantiate all the stepdef classes for java8 - the stepdef will be initialised
        // in the constructor.
        try {
            INSTANCE.set(this);
            for (Class<? extends LambdaGlue> lambdaGlueClass: lambdaGlueClasses) {
                objectFactory.getInstance(lambdaGlueClass);
            }
        } finally {
            INSTANCE.remove();
        }
    }

    @Override
    public void disposeWorld() {
        objectFactory.stop();
    }

    @Override
    public List<String> getSnippet(PickleStep step, String keyword, SnippetType.FunctionNameGenerator functionNameGenerator) {
        List<String> snippets = new ArrayList<>();
        snippets.addAll(annotationSnippetGenerator.getSnippet(step, keyword, functionNameGenerator));
        snippets.addAll(lambdaSnippetGenerator.getSnippet(step, keyword, functionNameGenerator));
        return snippets;
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

    @Override
    public void addStepDefinition(Function<TypeRegistry, StepDefinition> stepDefinitionFunction) {
        glue.addStepDefinition(stepDefinitionFunction.apply(typeRegistry));
    }

    void addHook(Annotation annotation, Method method) {
        if (objectFactory.addClass(method.getDeclaringClass())) {
            if (annotation.annotationType().equals(Before.class)) {
                String tagExpression = ((Before) annotation).value();
                long timeout = ((Before) annotation).timeout();
                addBeforeHookDefinition(new JavaHookDefinition(method, tagExpression, ((Before) annotation).order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(After.class)) {
                String tagExpression = ((After) annotation).value();
                long timeout = ((After) annotation).timeout();
                addAfterHookDefinition(new JavaHookDefinition(method, tagExpression, ((After) annotation).order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(BeforeStep.class)) {
                String tagExpression = ((BeforeStep) annotation).value();
                long timeout = ((BeforeStep) annotation).timeout();
                addBeforeStepHookDefinition(new JavaHookDefinition(method, tagExpression, ((BeforeStep) annotation).order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(AfterStep.class)) {
                String tagExpression = ((AfterStep) annotation).value();
                long timeout = ((AfterStep) annotation).timeout();
                addAfterStepHookDefinition(new JavaHookDefinition(method, tagExpression, ((AfterStep) annotation).order(), timeout, objectFactory));
            }
        }
    }

    @Override
    public void addBeforeHookDefinition(HookDefinition beforeHook) {
        glue.addBeforeHook(beforeHook);
    }

    @Override
    public void addAfterHookDefinition(HookDefinition afterHook) {
        glue.addAfterHook(afterHook);
    }

    @Override
    public void addAfterStepHookDefinition(HookDefinition afterStepHook) {
        glue.addAfterStepHook(afterStepHook);
    }

    @Override
    public void addBeforeStepHookDefinition(HookDefinition beforeStepHook) {
        glue.addBeforeStepHook(beforeStepHook);

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
