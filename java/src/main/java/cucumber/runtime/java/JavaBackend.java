package cucumber.runtime.java;

import cucumber.annotation.Pending;
import cucumber.classpath.Classpath;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.StepDefinition;
import cuke4duke.internal.Utils;
import gherkin.formatter.model.Step;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class JavaBackend implements Backend {
    private final ObjectFactory objectFactory;
    private List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();

    public JavaBackend(String packagePrefix) {
        this.objectFactory = Classpath.instantiateExactlyOneSubclass(ObjectFactory.class, "cucumber.runtime");
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

    void addStepDefinition(Pattern pattern, Method method, Locale locale) {
        Class<?> clazz = method.getDeclaringClass();
        objectFactory.addClass(clazz);
        stepDefinitions.add(new JavaStepDefinition(pattern, method, objectFactory, locale));
    }
    
    public Object invoke(Method method, Object[] javaArgs) {
		try {
			if (method.isAnnotationPresent(Pending.class)) {
				throw new CucumberException(method.getAnnotation(Pending.class).value());
			} else {
				return method.invoke(this.objectFactory.getInstance(method.getDeclaringClass()), javaArgs);
			}
		} catch (IllegalArgumentException e) {
			String m = "Couldn't invokeWithArgs " + method.toGenericString() + " with "
					+ Utils.join(javaArgs, ",");
			throw new CucumberException(m);
		} catch (InvocationTargetException e) {
			throw new CucumberException("Couldn't invoke method", e.getTargetException());
		} catch (IllegalAccessException e) {
			throw new CucumberException("Couldn't invoke method", e);
		}
	}
}
