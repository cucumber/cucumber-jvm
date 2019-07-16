package cucumber.runtime.java;

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
import io.cucumber.stepexpression.TypeRegistry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static cucumber.runtime.java.ObjectFactoryLoader.loadObjectFactory;
import static java.lang.Thread.currentThread;

public class JavaBackend implements Backend, LambdaGlueRegistry {
    
    static final String OBJECT_FACTORY_KEY = "cucumber.object-factory";

    /**
     * Returns a specified class name for an {@link io.cucumber.core.backend.ObjectFactory} or null.
     * An ObjectFactory might be specified in the options using the cucumber.object-factory key.
     */
    static String getObjectFactoryClassName(Env env)
    {
        return env.get(OBJECT_FACTORY_KEY);
    }

    /**
     * Returns a specified class name for an {@link cucumber.api.java.ObjectFactory} or null.
     * An ObjectFactory might be specified in the options using the classname of {@link cucumber.api.java.ObjectFactory} as key.
     */
    @Deprecated
    static String getDeprecatedObjectFactoryClassName(Env env)
    {
        return env.get(ObjectFactory.class.getName());
    }


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

    /**
     * This constructor will create an object factory.
     * The default object factory will be used unless an object factory of type {@link io.cucumber.core.backend.ObjectFactory} is specified
     * by its class name in the cucumber.properties under the JavaBackend.OBJECT_FACTORY_KEY key.
     * Alternatively a deprecated object factory of type {@link cucumber.api.java.ObjectFactory} can be specified  in cucumber.properties
     * under the key ObjectFactory.class.getName().
     */
    JavaBackend(ClassFinder classFinder, TypeRegistry typeRegistry) {
        this(loadObjectFactory(classFinder, getObjectFactoryClassName(Env.INSTANCE),getDeprecatedObjectFactoryClassName(Env.INSTANCE)), classFinder, typeRegistry);
    }

    JavaBackend(ObjectFactory objectFactory, ClassFinder classFinder,  TypeRegistry typeRegistry) {
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
            if (annotation.annotationType().equals(io.cucumber.java.Before.class)) {
                io.cucumber.java.Before before = (io.cucumber.java.Before) annotation;
                String[] tagExpressions = new String[]{before.value()};
                long timeout = before.timeout();
                addBeforeHookDefinition(new JavaHookDefinition(method, tagExpressions, before.order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(io.cucumber.java.After.class)) {
                io.cucumber.java.After after = (io.cucumber.java.After) annotation;
                String[] tagExpressions = new String[]{after.value()};
                long timeout = after.timeout();
                addAfterHookDefinition(new JavaHookDefinition(method, tagExpressions, after.order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(io.cucumber.java.BeforeStep.class)) {
                io.cucumber.java.BeforeStep beforeStep = (io.cucumber.java.BeforeStep) annotation;
                String[] tagExpressions = new String[]{beforeStep.value()};
                long timeout = beforeStep.timeout();
                addBeforeStepHookDefinition(new JavaHookDefinition(method, tagExpressions, beforeStep.order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(io.cucumber.java.AfterStep.class)) {
                io.cucumber.java.AfterStep afterStep = (io.cucumber.java.AfterStep) annotation;
                String[] tagExpressions = new String[]{afterStep.value()};
                long timeout = afterStep.timeout();
                addAfterStepHookDefinition(new JavaHookDefinition(method, tagExpressions, afterStep.order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(Before.class)) {
                Before before = (Before) annotation;
                String[] tagExpressions = before.value();
                long timeout = before.timeout();
                addBeforeHookDefinition(new JavaHookDefinition(method, tagExpressions, before.order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(After.class)) {
                After after = (After) annotation;
                String[] tagExpressions = after.value();
                long timeout = after.timeout();
                addAfterHookDefinition(new JavaHookDefinition(method, tagExpressions, after.order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(BeforeStep.class)) {
                BeforeStep beforeStep = (BeforeStep) annotation;
                String[] tagExpressions = beforeStep.value();
                long timeout = beforeStep.timeout();
                addBeforeStepHookDefinition(new JavaHookDefinition(method, tagExpressions, beforeStep.order(), timeout, objectFactory));
            } else if (annotation.annotationType().equals(AfterStep.class)) {
                AfterStep afterStep = (AfterStep) annotation;
                String[] tagExpressions = afterStep.value();
                long timeout = afterStep.timeout();
                addAfterStepHookDefinition(new JavaHookDefinition(method, tagExpressions, afterStep.order(), timeout, objectFactory));
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
