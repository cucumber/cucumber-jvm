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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class JavaBackend implements Backend {
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new JavaSnippet());
    private final Set<Class> stepDefinitionClasses = new HashSet<Class>();
    private final ObjectFactory objectFactory;
    private final ClasspathMethodScanner classpathMethodScanner = new ClasspathMethodScanner();
    private Glue glue;

    public JavaBackend(ResourceLoader ignored) {
        ObjectFactory foundOF;
        if (ObjectFactoryHolder.getFactory() != null) {
            foundOF = ObjectFactoryHolder.getFactory();
        } else {
            try {
                foundOF = new ClasspathResourceLoader().instantiateExactlyOneSubclass(ObjectFactory.class, "cucumber/runtime", new Class[0], new Object[0]);
            } catch (CucumberException ce) {
                foundOF = new DefaultJavaObjectFactory();
            }
        }
        objectFactory = foundOF;
    }

    public JavaBackend(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
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
                registerClassInObjectFactory(clazz);
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
        registerClassInObjectFactory(clazz);

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

    private void registerClassInObjectFactory(Class<?> clazz) {
        if (!stepDefinitionClasses.contains(clazz)) {
            objectFactory.addClass(clazz);
            stepDefinitionClasses.add(clazz);
            addConstructorDependencies(clazz);
        }
    }

    private void addConstructorDependencies(Class<?> clazz) {
        for (Constructor constructor : clazz.getConstructors()) {
            for (Class paramClazz : constructor.getParameterTypes()) {
                registerClassInObjectFactory(paramClazz);
            }
        }
    }
}
