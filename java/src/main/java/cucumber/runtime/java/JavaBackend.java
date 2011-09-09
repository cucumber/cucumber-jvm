package cucumber.runtime.java;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import cucumber.annotation.Order;
import cucumber.resources.Resources;
import cucumber.runtime.Backend;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;
import gherkin.formatter.model.Step;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class JavaBackend implements Backend {
    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private final List<HookDefinition> beforeHooks = new ArrayList<HookDefinition>();
    private final List<HookDefinition> afterHooks = new ArrayList<HookDefinition>();
    private final Set<Class> stepDefinitionClasses = new HashSet<Class>();
    private final ObjectFactory objectFactory;

    public JavaBackend(List<String> packagePrefixes) {
        this.objectFactory = Resources.instantiateExactlyOneSubclass(ObjectFactory.class, "cucumber.runtime", new Class[0], new Object[0]);
        ClasspathMethodScanner classpathMethodScanner = new ClasspathMethodScanner();
        for (String packagePrefix : packagePrefixes) {
            classpathMethodScanner.scan(this, packagePrefix);
        }
    }

    public JavaBackend(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    @Override
    public List<HookDefinition> getBeforeHooks() {
        return beforeHooks;
    }

    @Override
    public List<HookDefinition> getAfterHooks() {
        return afterHooks;
    }

    public void newWorld() {
        objectFactory.createInstances();
    }

    public void disposeWorld() {
        objectFactory.disposeInstances();
    }

    public String getSnippet(Step step) {
        return new JavaSnippetGenerator(step).getSnippet();
    }

    void addStepDefinition(Pattern pattern, Method method) {
        Class<?> clazz = method.getDeclaringClass();
        registerClassInObjectFactory(clazz);
        stepDefinitions.add(new JavaStepDefinition(pattern, method, objectFactory));
    }

    void registerHook(Annotation annotation, Method method) {
        Class<?> clazz = method.getDeclaringClass();
        registerClassInObjectFactory(clazz);

        Order order = method.getAnnotation(Order.class);
        int hookOrder = (order == null) ? Integer.MAX_VALUE : order.value();

        if (annotation.annotationType().equals(Before.class)) {
            String[] tagExpressions = ((Before) annotation).value();
            beforeHooks.add(new JavaHookDefinition(method, tagExpressions, hookOrder, objectFactory));
        } else {
            String[] tagExpressions = ((After) annotation).value();
            afterHooks.add(new JavaHookDefinition(method, tagExpressions, hookOrder, objectFactory));
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
