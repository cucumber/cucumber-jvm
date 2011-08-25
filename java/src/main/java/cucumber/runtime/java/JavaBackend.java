package cucumber.runtime.java;

import gherkin.formatter.model.Step;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cucumber.annotation.Before;
import cucumber.annotation.Pending;
import cucumber.resources.Resources;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.PendingException;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;

public class JavaBackend implements Backend {
    private final ObjectFactory objectFactory;
    private List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private List<HookDefinition> beforeHooks = new ArrayList<HookDefinition>();
	private List<HookDefinition> afterHooks = new ArrayList<HookDefinition>();

    public JavaBackend(String packagePrefix) {
        this.objectFactory = Resources.instantiateExactlyOneSubclass(ObjectFactory.class, "cucumber.runtime");
        new ClasspathMethodScanner().scan(this, packagePrefix);
    }

    public JavaBackend(ObjectFactory objectFactory, List<StepDefinition> stepDefinitions) {
        this.objectFactory = objectFactory;
        this.stepDefinitions = stepDefinitions;
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
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
        objectFactory.addClass(clazz);
        stepDefinitions.add(new JavaStepDefinition(pattern, method, objectFactory));
    }

    public Object invoke(Method method, Object[] javaArgs) {
        try {
            if (method.isAnnotationPresent(Pending.class)) {
                throw new PendingException(method.getAnnotation(Pending.class).value());
            } else {
                return method.invoke(this.objectFactory.getInstance(method.getDeclaringClass()), javaArgs);
            }
        } catch (IllegalArgumentException e) {
            throw new CucumberException(errorMessage(method, javaArgs), e);
        } catch (InvocationTargetException e) {
            throw new CucumberException(errorMessage(method, javaArgs), e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new CucumberException(errorMessage(method, javaArgs), e);
        }
    }

    private String errorMessage(Method method, Object[] javaArgs) {
        StringBuilder m = new StringBuilder("Couldn't invoke ").append(method.toGenericString()).append(" with ").append(Utils.join(javaArgs, ",")).append(" (");
        boolean comma = false;
        for (Object javaArg : javaArgs) {
            if (comma) m.append(",");
            m.append(javaArg.getClass());
            comma = true;
        }
        m.append(")");
        return m.toString();
    }

	void registerHook(Annotation annotation, Method method) {
		Class<?> clazz = method.getDeclaringClass();
        objectFactory.addClass(clazz);

        if (annotation.annotationType().equals(Before.class)) {
        	beforeHooks.add(new JavaHookDefinition(method, objectFactory));
        } else {
        	afterHooks.add(new JavaHookDefinition(method, objectFactory));
        }
	}

	@Override
	public List<HookDefinition> getBeforeHooks() {
		return beforeHooks;
	}

	@Override
	public List<HookDefinition> getAfterHooks() {		
		return afterHooks;
	}
}
