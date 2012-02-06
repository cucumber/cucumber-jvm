package cucumber.runtime.java;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.annotation.Order;
import cucumber.fallback.runtime.java.DefaultJavaObjectFactory;
import cucumber.io.ClasspathResourceLoader;
import cucumber.io.ResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

public class JavaBackend implements Backend {
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new JavaSnippet());
    private final ObjectFactory objectFactory;
    private final ClasspathResourceLoader classpathResourceLoader;
    private final ClasspathMethodScanner classpathMethodScanner;
    private Glue glue;

    public JavaBackend(ResourceLoader ignored) {
        classpathResourceLoader = new ClasspathResourceLoader(Thread.currentThread().getContextClassLoader());
        classpathMethodScanner = new ClasspathMethodScanner(classpathResourceLoader);
        objectFactory = loadObjectFactory();
    }

    public JavaBackend(ObjectFactory objectFactory) {
        classpathResourceLoader = new ClasspathResourceLoader(Thread.currentThread().getContextClassLoader());
        classpathMethodScanner = new ClasspathMethodScanner(classpathResourceLoader);
        this.objectFactory = objectFactory;
    }

    private ObjectFactory loadObjectFactory() {
        ObjectFactory foundOF;
        if (ObjectFactoryHolder.getFactory() != null) {
            foundOF = ObjectFactoryHolder.getFactory();
        } else {
            try {
                foundOF = classpathResourceLoader.instantiateExactlyOneSubclass(ObjectFactory.class, "cucumber.runtime", new Class[0], new Object[0]);
            } catch (CucumberException ce) {
                foundOF = new DefaultJavaObjectFactory();
            }
        }
        return foundOF;
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        this.glue = glue;
        classpathMethodScanner.scan(this, gluePaths);
    }

    @Override
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
        //Not used here yet
    }

    @Override
    public void buildWorld() {
        objectFactory.createInstances();
    }

    @Override
    public void disposeWorld() {
        objectFactory.disposeInstances();
    }

    @Override
    public String getSnippet(Step step) {
        return snippetGenerator.getSnippet(step);
    }

    void addStepDefinition(Annotation annotation, Method method) {
        try {
            Method regexpMethod = annotation.getClass().getMethod("value");
            String regexpString = (String) regexpMethod.invoke(annotation);
            if (regexpString != null) {
                Pattern pattern = Pattern.compile(regexpString);
                Class<?> clazz = method.getDeclaringClass();
                objectFactory.addClass(clazz);
                glue.addStepDefinition(new JavaStepDefinition(method, pattern, objectFactory));
            }
        } catch (NoSuchMethodException e) {
            throw new CucumberException(e);
        } catch (InvocationTargetException e) {
            throw new CucumberException(e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new CucumberException(e);
        }
    }

    void addHook(Annotation annotation, Method method) {
        Class<?> clazz = method.getDeclaringClass();
        objectFactory.addClass(clazz);

        Order order = method.getAnnotation(Order.class);
        int hookOrder = (order == null) ? Integer.MAX_VALUE : order.value();

        if (annotation.annotationType().equals(Before.class)) {
            String[] tagExpressions = ((Before) annotation).value();
            glue.addBeforeHook(new JavaHookDefinition(method, tagExpressions, hookOrder, objectFactory));
        } else {
            String[] tagExpressions = ((After) annotation).value();
            glue.addAfterHook(new JavaHookDefinition(method, tagExpressions, hookOrder, objectFactory));
        }
    }
}
