package cuke4duke.junit;

import cuke4duke.Feature;
import cuke4duke.Scenario;
import cuke4duke.internal.CucumberRunner;
import cuke4duke.internal.Visitor;
import org.apache.bsf.BSFException;
import org.jruby.RubyException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class CucumberJunit4Runner extends BlockJUnit4ClassRunner {
    private final CucumberRunner cucumberRunner;

    private final List<FrameworkMethod> scenarioMethods;
    private final String featurePath;

    public CucumberJunit4Runner(Class<?> featureClass) throws org.junit.runners.model.InitializationError, BSFException {
        super(featureClass);
        featurePath = featurePathFor(featureClass);
        scenarioMethods = extractScenarios(featureClass);
        cucumberRunner = null; //ew CucumberRunner(stepMother);
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        List<FrameworkMethod> children = new ArrayList<FrameworkMethod>();
        children.addAll(super.getChildren());
        children.addAll(scenarioMethods);
        return children;
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        if (scenarioMethods.contains(method)) {
            return scenarioStatement((CucumberScenarioMethod) method);
        }
        return super.methodInvoker(method, test);
    }

    @Override
    protected void validateInstanceMethods(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(After.class, false, errors);
        validatePublicVoidNoArgMethods(Before.class, false, errors);
        validateTestMethods(errors);
    }

    protected Statement scenarioStatement(final CucumberScenarioMethod method) {
        return new Statement() {
            public void evaluate() throws Throwable {
                JUnitVisitor visitor = new JUnitVisitor();
                cucumberRunner.run(featurePath, method.getName(), visitor);
                if(visitor.getException() != null) {
                    throw visitor.getException();
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
	private List<FrameworkMethod> extractScenarios(Class featureClass) {
        List<FrameworkMethod> scenarioMethods = new ArrayList<FrameworkMethod>();
        for (Method method : featureClass.getMethods()) {
            Scenario scenarioAnnotation = method.getAnnotation(Scenario.class);
            Ignore ignoreAnnotation = method.getAnnotation(Ignore.class);
            if (scenarioAnnotation != null && ignoreAnnotation == null) {
                String scenarioName = extractScenarioName(method, scenarioAnnotation);
                scenarioMethods.add(new CucumberScenarioMethod(method, scenarioName));
            }
        }
        return scenarioMethods;
    }

    private String extractScenarioName(Method method, Scenario scenarioAnnotation) {
        return scenarioAnnotation.value().length() == 0 ? method.getName().replace("_", " ") : scenarioAnnotation.value();
    }

    @SuppressWarnings("unchecked")
    private String featurePathFor(Class featureClass) {
        Feature featureAnnotation = (Feature) featureClass.getAnnotation(Feature.class);
        return featureAnnotation != null ? featureAnnotation.value() : featureClass.getSimpleName() + ".feature";
    }

    private static class CucumberScenarioMethod extends FrameworkMethod {
        private final String scenarioName;

        public CucumberScenarioMethod(Method scenarioMethod, String scenarioName) {
            super(scenarioMethod);
            this.scenarioName = scenarioName;
        }

        @Override
        public String getName() {
            return scenarioName;
        }
    }

    private class JUnitVisitor implements Visitor {
        private Exception exception;

        public void visitFeatures() {
        }

        public void visitScenarioName(String keyword, String scenarioName) {
        }

        public void visitStepResult(String keyword, String status, RubyException exception) {
            if(exception != null && this.exception == null) {
                this.exception = new Exception(exception.message.toString());
            }
        }

        public Exception getException() {
            return exception;
        }
    }
}
