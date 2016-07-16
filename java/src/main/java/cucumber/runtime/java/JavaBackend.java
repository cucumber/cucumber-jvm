package cucumber.runtime.java;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.ObjectFactory;
import cucumber.api.java8.GlueBase;
import cucumber.api.java8.HookBody;
import cucumber.api.java8.HookNoArgsBody;
import cucumber.api.java8.StepdefBody;
import cucumber.runtime.Backend;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.DuplicateStepDefinitionException;
import cucumber.runtime.Env;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.Utils;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.Snippet;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.pickles.PickleStep;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static cucumber.runtime.io.MultiLoader.packageName;

public class JavaBackend implements Backend {
    public static final ThreadLocal<JavaBackend> INSTANCE = new ThreadLocal<JavaBackend>();
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(createSnippet());

    private Snippet createSnippet() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
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
    public JavaBackend(ResourceLoader resourceLoader) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        methodScanner = new MethodScanner(classFinder);
        objectFactory = ObjectFactoryLoader.loadObjectFactory(classFinder, Env.INSTANCE.get(ObjectFactory.class.getName()));
    }

    public JavaBackend(ObjectFactory objectFactory) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        methodScanner = new MethodScanner(classFinder);
        this.objectFactory = objectFactory;
    }

    public JavaBackend(ObjectFactory objectFactory, ClassFinder classFinder) {
        this.objectFactory = objectFactory;
        this.classFinder = classFinder;
        methodScanner = new MethodScanner(classFinder);
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        this.glue = glue;
        // Scan for Java7 style glue (annotated methods)
        methodScanner.scan(this, gluePaths);

        // Scan for Java8 style glue (lambdas)
        for (final String gluePath : gluePaths) {
            Collection<Class<? extends GlueBase>> glueDefinerClasses = classFinder.getDescendants(GlueBase.class, packageName(gluePath));
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
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
        //Not used here yet
    }

    @Override
    public void buildWorld() {
        objectFactory.start();

        // Instantiate all the stepdef classes for java8 - the stepdef will be initialised
        // in the constructor.
        try {
            INSTANCE.set(this);
            glue.removeScenarioScopedGlue();
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
    public String getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        return snippetGenerator.getSnippet(step, keyword, functionNameGenerator);
    }

    void addStepDefinition(Annotation annotation, Method method) {
        try {
            if (objectFactory.addClass(method.getDeclaringClass())) {
                glue.addStepDefinition(new JavaStepDefinition(method, pattern(annotation), timeoutMillis(annotation), objectFactory));
            }
        } catch (DuplicateStepDefinitionException e) {
            throw e;
        } catch (Throwable e) {
            throw new CucumberException(e);
        }
    }

    public void addStepDefinition(String regexp, long timeoutMillis, StepdefBody body, TypeIntrospector typeIntrospector) {
        try {
            glue.addStepDefinition(new Java8StepDefinition(Pattern.compile(regexp), timeoutMillis, body, typeIntrospector));
        } catch (CucumberException e) {
            throw e;
        } catch (Exception e) {
            throw new CucumberException(e);
        }
    }

    void addHook(Annotation annotation, Method method) {
        if (objectFactory.addClass(method.getDeclaringClass())) {
            if (annotation.annotationType().equals(Before.class)) {
                String[] tagExpressions = ((Before) annotation).value();
                long timeout = ((Before) annotation).timeout();
                glue.addBeforeHook(new JavaHookDefinition(method, tagExpressions, ((Before) annotation).order(), timeout, objectFactory));
            } else {
                String[] tagExpressions = ((After) annotation).value();
                long timeout = ((After) annotation).timeout();
                glue.addAfterHook(new JavaHookDefinition(method, tagExpressions, ((After) annotation).order(), timeout, objectFactory));
            }
        }
    }

    public void addBeforeHookDefinition(String[] tagExpressions, long timeoutMillis, int order, HookBody body) {
        glue.addBeforeHook(new Java8HookDefinition(tagExpressions, order, timeoutMillis, body));
    }

    public void addAfterHookDefinition(String[] tagExpressions, long timeoutMillis, int order, HookBody body) {
        glue.addAfterHook(new Java8HookDefinition(tagExpressions, order, timeoutMillis, body));
    }

    public void addBeforeHookDefinition(String[] tagExpressions, long timeoutMillis, int order, HookNoArgsBody body) {
        glue.addBeforeHook(new Java8HookDefinition(tagExpressions, order, timeoutMillis, body));
    }

    public void addAfterHookDefinition(String[] tagExpressions, long timeoutMillis, int order, HookNoArgsBody body) {
        glue.addAfterHook(new Java8HookDefinition(tagExpressions, order, timeoutMillis, body));
    }

    private Pattern pattern(Annotation annotation) throws Throwable {
        Method regexpMethod = annotation.getClass().getMethod("value");
        String regexpString = (String) Utils.invoke(annotation, regexpMethod, 0);
        return Pattern.compile(regexpString);
    }

    private long timeoutMillis(Annotation annotation) throws Throwable {
        Method regexpMethod = annotation.getClass().getMethod("timeout");
        return (Long) Utils.invoke(annotation, regexpMethod, 0);
    }

}
