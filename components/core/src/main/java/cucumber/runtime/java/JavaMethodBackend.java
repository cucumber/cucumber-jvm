package cucumber.runtime.java;

import cucumber.StepDefinition;
import cucumber.runtime.Backend;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class JavaMethodBackend implements Backend {
    private final ObjectFactory objectFactory;
    private List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();

    public JavaMethodBackend(ObjectFactory objectFactory, MethodScanner methodScanner, String packagePrefix) {
        this.objectFactory = objectFactory;
        methodScanner.scan(this, packagePrefix);
    }

    // TODO: Use Set
    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    void addStepDefinition(Pattern pattern, Method method, Locale locale) {
        Class<?> clazz = method.getDeclaringClass();
        objectFactory.addClass(clazz);
        stepDefinitions.add(new JavaMethodStepDefinition(pattern, method, objectFactory, locale));
    }

    public void newScenario() {
        objectFactory.createObjects();
    }
}
