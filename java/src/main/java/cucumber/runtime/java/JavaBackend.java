package cucumber.runtime.java;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java8.HookBody;
import cucumber.api.java8.HookNoArgsBody;
import cucumber.api.java8.Language;
import cucumber.api.java8.StepdefBody;
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
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static cucumber.runtime.io.MultiLoader.packageName;

public class JavaBackend implements Backend {
    public static final ThreadLocal<JavaBackend> INSTANCE = new ThreadLocal<JavaBackend>();
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
        // Scan for Java7 style glue (annotated methods)
        methodScanner.scan(this, gluePaths);

        // Scan for Java8 style glue (lambdas)
        for (final String gluePath : gluePaths) {
            Collection<Class<? extends Language>> glueDefinerClasses = classFinder.getDescendants(Language.class, packageName(gluePath));
            for (final Class<? extends Language> glueClass : glueDefinerClasses) {
                if (glueClass.isInterface()) {
                    continue;
                }

                objectFactory.addClass(glueClass);
            }
            INSTANCE.set(this);
            try {
                for (Class<? extends Language> glueDefinerClass : glueDefinerClasses) {
                    objectFactory.getInstance(glueDefinerClass).defineGlue();
                }
            } catch (Exception e) {
                throw new CucumberException("Failed to instantiate Java8 glue", e);
            } finally {
                INSTANCE.remove();
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

    public void addStepDefinition(String regexp, long timeoutMillis, StepdefBody body, TypeIntrospector typeIntrospector) {
        try {
            glue.addStepDefinition(new Java8StepDefinition(Pattern.compile(regexp), timeoutMillis, body, typeIntrospector));
        } catch (Exception e) {
            throw new CucumberException(e);
        }
    }

    void addHook(Annotation annotation, Method method) {
        objectFactory.addClass(method.getDeclaringClass());

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

    public void addBeforeHookDefinition(long timeoutMillis, HookBody body) {
        glue.addBeforeHook(new Java8HookDefinition(body, new String[0], 0, timeoutMillis, objectFactory));
    }

    public void addAfterHookDefinition(long timeoutMillis, HookBody body) {
        glue.addAfterHook(new Java8HookDefinition(body, new String[0], 10000, timeoutMillis, objectFactory));
    }

    public void addBeforeHookDefinition(long timeoutMillis, HookNoArgsBody body) {
        glue.addBeforeHook(new Java8HookDefinition(body, new String[0], 0, timeoutMillis, objectFactory));
    }

    public void addAfterHookDefinition(long timeoutMillis, HookNoArgsBody body) {
        glue.addAfterHook(new Java8HookDefinition(body, new String[0], 10000, timeoutMillis, objectFactory));
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

    private static String getMultipleObjectFactoryLogMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("More than one Cucumber ObjectFactory was found in the classpath\n\n");
        sb.append("You probably may have included, for instance, cucumber-spring AND cucumber-guice as part of\n");
        sb.append("your dependencies. When this happens, Cucumber falls back to instantiating the\n");
        sb.append("DefaultJavaObjectFactory implementation which doesn't provide IoC.\n");
        sb.append("In order to enjoy IoC features, please remove the unnecessary dependencies from your classpath.\n");
        return sb.toString();
    }
}
