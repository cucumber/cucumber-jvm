package cucumber.runtime.java;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.AfterFeature;
import cucumber.api.java.BeforeFeature;
import cucumber.api.java.AfterAll;
import cucumber.api.java.BeforeAll;
import cucumber.runtime.Backend;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.DuplicateStepDefinitionException;
import cucumber.runtime.Glue;
import cucumber.runtime.NoInstancesException;
import cucumber.runtime.Reflections;
import cucumber.runtime.TooManyInstancesException;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.Utils;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

public class JavaBackend implements Backend {
    private SnippetGenerator snippetGenerator = new SnippetGenerator(new JavaSnippet());
    private final ObjectFactory objectFactory;
    private final ClassFinder classFinder;

    private final MethodScanner methodScanner;
    private Glue glue;

    /**
     * The constructor called by reflection by default.
     *
     * @param resourceLoader
     */
    public JavaBackend(ResourceLoader resourceLoader) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        methodScanner = new MethodScanner(classFinder);
        objectFactory = loadObjectFactory(classFinder);
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

    public static ObjectFactory loadObjectFactory(ClassFinder classFinder) {
        ObjectFactory objectFactory;
        try {
            Reflections reflections = new Reflections(classFinder);
            objectFactory = reflections.instantiateExactlyOneSubclass(ObjectFactory.class, "cucumber.runtime", new Class[0], new Object[0]);
        } catch (TooManyInstancesException e) {
            System.out.println(getMultipleObjectFactoryLogMessage());
            objectFactory = new DefaultJavaObjectFactory();
        } catch (NoInstancesException e) {
            objectFactory = new DefaultJavaObjectFactory();
        }
        return objectFactory;
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        this.glue = glue;
        methodScanner.scan(this, gluePaths);
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
    }

    @Override
    public void disposeWorld() {
        objectFactory.stop();
    }

    @Override
    public String getSnippet(Step step, FunctionNameGenerator functionNameGenerator) {
        return snippetGenerator.getSnippet(step, functionNameGenerator);
    }

    void addStepDefinition(Annotation annotation, Method method) {
        try {
            objectFactory.addClass(method.getDeclaringClass());
            glue.addStepDefinition(new JavaStepDefinition(method, pattern(annotation), timeoutMillis(annotation), objectFactory));
        } catch (DuplicateStepDefinitionException e) {
            throw e;
        } catch (Throwable e) {
            throw new CucumberException(e);
        }
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

    void addHook(Annotation annotation, Method method) {
        objectFactory.addClass(method.getDeclaringClass());

        HookType hookType = HookType.fromAnnotation(annotation);
        HookOptions hookOptions;
        switch (hookType) {
            case BEFORE_ALL:
                BeforeAll beforeAllHook = (BeforeAll) annotation;
                hookOptions = new HookOptions(beforeAllHook.order(), beforeAllHook.timeout());
                glue.addBeforeAllHook(createJavaHook(hookOptions, method));
                break;
            case AFTER_ALL:
                AfterAll afterAllHook = (AfterAll) annotation;
                hookOptions = new HookOptions(afterAllHook.order(), afterAllHook.timeout());
                glue.addAfterAllHook(createJavaHook(hookOptions, method));
                break;
            case BEFORE_FEATURE:
                BeforeFeature beforeFeatureHook = (BeforeFeature) annotation;
                hookOptions = new HookOptions(beforeFeatureHook.order(), beforeFeatureHook.value(), beforeFeatureHook.timeout());
                glue.addBeforeFeatureHook(createJavaHook(hookOptions, method));
                break;
            case AFTER_FEATURE:
                AfterFeature afterFeatureHook = (AfterFeature) annotation;
                hookOptions = new HookOptions(afterFeatureHook.order(), afterFeatureHook.value(), afterFeatureHook.timeout());
                glue.addAfterFeatureHook(createJavaHook(hookOptions, method));
                break;
            case BEFORE:
                Before beforeHook = (Before) annotation;
                hookOptions = new HookOptions(beforeHook.order(), beforeHook.value(), beforeHook.timeout());
                glue.addBeforeHook(createJavaHook(hookOptions, method));
                break;
            case AFTER:
                After afterHook = (After) annotation;
                hookOptions = new HookOptions(afterHook.order(), afterHook.value(), afterHook.timeout());
                glue.addAfterHook(createJavaHook(hookOptions, method));
                break;
            default: throw new IllegalArgumentException("Unknown hook type" + hookType);
        }
    }

    private JavaHookDefinition createJavaHook(HookOptions hookOptions, Method method) {
        String[] tagExpressions = hookOptions.tags;
        long timeout = hookOptions.timeout;
        int order = hookOptions.order;
        return new JavaHookDefinition(method, tagExpressions, order, timeout, objectFactory);
    }

    private static String getMultipleObjectFactoryLogMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("More than one Cucumber ObjectFactory was found in the classpath\n\n");
        sb.append("You probably may have included, for instance, cucumber-spring AND cucumber-guice as part of\n");
        sb.append("your dependencies. When this happens, Cucumber falls back to instantiating the\n");
        sb.append("DefaultJavaObjectFactory implementation which doesn't provide IoC.\n");
        sb.append("In order to enjoy IoC features, please remove the unnecessary dependencies from your classpath.\n");
        return sb.toString();
    }

    private static class HookOptions {
        String[] tags = {};
        int order;
        long timeout;

        private HookOptions(int order, String[] tags, long timeout) {
            this.order = order;
            this.tags = tags;
            this.timeout = timeout;
        }

        private HookOptions(int order, long timeout) {
            this.order = order;
            this.timeout = timeout;
        }
    }
}
