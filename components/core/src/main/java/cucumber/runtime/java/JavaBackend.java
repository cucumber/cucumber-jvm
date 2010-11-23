package cucumber.runtime.java;

import cucumber.runtime.StepDefinition;
import cucumber.runtime.Backend;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class JavaBackend implements Backend {
    private final ObjectFactory objectFactory;
    private List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();

    public JavaBackend(ObjectFactory objectFactory, MethodScanner methodScanner, String packagePrefix) {
        this.objectFactory = objectFactory;
        methodScanner.scan(this, packagePrefix);
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    void addStepDefinition(Pattern pattern, Method method, Locale locale) {
        Class<?> clazz = method.getDeclaringClass();
        objectFactory.addClass(clazz);
        stepDefinitions.add(new JavaStepDefinition(pattern, method, objectFactory, locale));
    }

    public void newScenario() {
        objectFactory.createObjects();
    }
}
