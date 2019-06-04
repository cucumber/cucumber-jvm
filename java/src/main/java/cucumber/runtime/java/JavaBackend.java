package cucumber.runtime.java;

import static cucumber.runtime.java.ObjectFactoryLoader.loadObjectFactory;
import static java.lang.Thread.currentThread;

import io.cucumber.stepexpression.TypeRegistry;
import cucumber.api.java.After;
import cucumber.api.java.AfterStep;
import cucumber.api.java.Before;
import cucumber.api.java.BeforeStep;
import cucumber.api.java.ObjectFactory;
import cucumber.api.java8.GlueBase;
import cucumber.runtime.Backend;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Env;
import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.Snippet;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.pickles.PickleStep;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JavaBackend implements Backend, LambdaGlueRegistry {

    private final SnippetGenerator snippetGenerator;
    private final TypeRegistry typeRegistry;

    private Snippet createSnippet() {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        try {
            classLoader.loadClass("cucumber.runtime.java8.LambdaGlueBase");
            return new Java8Snippet();
        } catch (ClassNotFoundException thatsOk) {
            return new JavaSnippet();
        }
    }

    private final ObjectFactory objectFactory;
    private final ClassFinder classFinder;

    private final MethodScanner methodScanner;
    private Glue glue;
    private List<Class<? extends GlueBase>> glueBaseClasses = new ArrayList<Class<? extends GlueBase>>();

    /**
     * The constructor called by reflection by default.
     *
     * @param resourceLoader
     */
    public JavaBackend(ResourceLoader resourceLoader, TypeRegistry typeRegistry) {
        this(new ResourceLoaderClassFinder(resourceLoader, currentThread().getContextClassLoader()), typeRegistry);
    }

    private JavaBackend(ClassFinder classFinder, TypeRegistry typeRegistry) {
        this(loadObjectFactory(classFinder, Env.INSTANCE.get(ObjectFactory.class.getName())), classFinder, typeRegistry);
    }

    public JavaBackend(ObjectFactory objectFactory, ClassFinder classFinder,  TypeRegistry typeRegistry) {
        this.classFinder = classFinder;
        this.objectFactory = objectFactory;
        this.methodScanner = new MethodScanner(classFinder);
        this.snippetGenerator = new SnippetGenerator(createSnippet(), typeRegistry.parameterTypeRegistry());
        this.typeRegistry = typeRegistry;
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        this.glue = glue;
        // Scan for Java7 style glue (annotated methods)
        methodScanner.scan(this, gluePaths);

        // Scan for Java8 style glue (lambdas)
        for (final URI gluePath : gluePaths) {
            Collection<Class<? extends GlueBase>> glueDefinerClasses = classFinder.getDescendants(GlueBase.class, gluePath);
            for (final Class<? extends GlueBase> glueClass : glueDefinerClasses) {
                if (glueClass.isInterface()) {
                    continue;
                }

                if (objectFactory.addClass(glueClass)) {
                    glueBaseClasses.add(glueClass);
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
            for (Class<? extends GlueBase> glueBaseClass : glueBaseClasses) {
                objectFactory.getInstance(glueBaseClass);
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
    public List<String> getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        return snippetGenerator.getSnippet(step, keyword, functionNameGenerator);
    }

    void addStepDefinition(Annotation annotation, Method method) {
        try {
            if (objectFactory.addClass(method.getDeclaringClass())) {
                glue.addStepDefinition(
                    new JavaStepDefinition(
                        method,
                        expression(annotation),
                        timeoutMillis(annotation),
                        objectFactory,
                        typeRegistry));
            }
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
                String[] tagExpressions = ((Before) annotation).value();
                long timeout = ((Before) annotation).timeout();
                addBeforeHookDefinition(new JavaHookDefinition(method, tagExpressions, ((Before) annotation).order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(After.class)) {
                String[] tagExpressions = ((After) annotation).value();
                long timeout = ((After) annotation).timeout();
                addAfterHookDefinition(new JavaHookDefinition(method, tagExpressions, ((After) annotation).order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(BeforeStep.class)) {
                String[] tagExpressions = ((BeforeStep) annotation).value();
                long timeout = ((BeforeStep) annotation).timeout();
                addBeforeStepHookDefinition(new JavaHookDefinition(method, tagExpressions, ((BeforeStep) annotation).order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(AfterStep.class)) {
                String[] tagExpressions = ((AfterStep) annotation).value();
                long timeout = ((AfterStep) annotation).timeout();
                addAfterStepHookDefinition(new JavaHookDefinition(method, tagExpressions, ((AfterStep) annotation).order(), timeout, objectFactory));
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
        return (String) Utils.invoke(annotation, expressionMethod, 0);
    }

    private long timeoutMillis(Annotation annotation) throws Throwable {
        Method regexpMethod = annotation.getClass().getMethod("timeout");
        return (Long) Utils.invoke(annotation, regexpMethod, 0);
    }

}
