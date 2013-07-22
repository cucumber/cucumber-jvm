package cucumber.runtime.java;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.runtime.*;
import cucumber.runtime.io.*;
import cucumber.runtime.snippets.SnippetGenerator;
import cucumber.runtime.snippets.UnderscoreFunctionNameSanitizer;
import gherkin.formatter.model.Step;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

public class JavaBackend implements SnippetTypeAwareBackend {
    private SnippetGenerator snippetGenerator = new SnippetGenerator(new JavaSnippet(), new UnderscoreFunctionNameSanitizer());
    private final ObjectFactory objectFactory;
    private final Reflections reflections;

    private final MethodScanner methodScanner;
    private Glue glue;

    /**
     * The constructor called by reflection by default.
     * @param resourceLoader
     */
    public JavaBackend(ResourceLoader resourceLoader) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        reflections = new ResourceLoaderReflections(resourceLoader, classLoader);
        methodScanner = new MethodScanner(reflections);
        objectFactory = loadObjectFactory();
    }

    public JavaBackend(ObjectFactory objectFactory) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        reflections = new ResourceLoaderReflections(resourceLoader, classLoader);
        methodScanner = new MethodScanner(reflections);
        this.objectFactory = objectFactory;
    }

    public JavaBackend(ObjectFactory objectFactory, Reflections reflections) {
        this.objectFactory = objectFactory;
        this.reflections = reflections;
        methodScanner = new MethodScanner(reflections);
    }

    private ObjectFactory loadObjectFactory() {
        ObjectFactory objectFactory;
        try {
            objectFactory = reflections.instantiateExactlyOneSubclass(ObjectFactory.class, "cucumber.runtime", new Class[0], new Object[0]);
        } catch (CucumberException ce) {
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
    public String getSnippet(Step step) {
        return snippetGenerator.getSnippet(step);
    }

    void addStepDefinition(Annotation annotation, Method method) {
        try {
            objectFactory.addClass(method.getDeclaringClass());
            glue.addStepDefinition(new JavaStepDefinition(method, pattern(annotation), timeout(annotation), objectFactory));
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

    private int timeout(Annotation annotation) throws Throwable {
        Method regexpMethod = annotation.getClass().getMethod("timeout");
        return (Integer) Utils.invoke(annotation, regexpMethod, 0);
    }

    void addHook(Annotation annotation, Method method) {
        objectFactory.addClass(method.getDeclaringClass());

        if (annotation.annotationType().equals(Before.class)) {
            String[] tagExpressions = ((Before) annotation).value();
            int timeout = ((Before) annotation).timeout();
            glue.addBeforeHook(new JavaHookDefinition(method, tagExpressions, ((Before) annotation).order(), timeout, objectFactory));
        } else {
            String[] tagExpressions = ((After) annotation).value();
            int timeout = ((After) annotation).timeout();
            glue.addAfterHook(new JavaHookDefinition(method, tagExpressions, ((After) annotation).order(), timeout, objectFactory));
        }
    }

    @Override
    public void setSnippetType(SnippetType type) {
        switch (type) {
            case CAMELCASE:
                snippetGenerator = new SnippetGenerator(new JavaSnippet(), new CamelCaseFunctionNameSanitizer());
                break;
            case UNDERSCORE:
                snippetGenerator = new SnippetGenerator(new JavaSnippet(), new UnderscoreFunctionNameSanitizer());
                break;
            default:
                throw new CucumberException(String.format("Unsupported Snippet Type: %s", type.toString()));

        }
    }
}
