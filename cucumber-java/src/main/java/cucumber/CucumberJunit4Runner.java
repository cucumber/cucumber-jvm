package cucumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public class CucumberJunit4Runner extends BlockJUnit4ClassRunner {
    private final List<FrameworkMethod> scenarioMethods = new ArrayList<FrameworkMethod>();
    private final String featureName;

    public CucumberJunit4Runner(Class<?> featureClass) throws org.junit.runners.model.InitializationError {
        super(featureClass);
        featureName = extractFeatureName(featureClass);
        System.out.println("Creating feature '" + featureName + "'");
        List<CucumberScenarioWrapper> cucumberScenarios = extractScenarios(featureClass);
        for (CucumberScenarioWrapper cucumberScenario : cucumberScenarios) {
            scenarioMethods.add(new CucumberScenarioMethod(cucumberScenario));
        }
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
            return executeScenario(test, (CucumberScenarioMethod) method);
        }
        return super.methodInvoker(method, test);
    }

    @Override
    protected void validateInstanceMethods(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(After.class, false, errors);
        validatePublicVoidNoArgMethods(Before.class, false, errors);
        validateTestMethods(errors);
    }


    protected Statement executeScenario(final Object featureObject, final CucumberScenarioMethod method) {
        return new Statement() {
            public void evaluate() throws Throwable {
                System.out.println("Executing scenario = " + method.getName());
            }
        };
    }

    private List<CucumberScenarioWrapper> extractScenarios(Class featureClass) {
        List<CucumberScenarioWrapper> cucumberScenarios = new ArrayList<CucumberScenarioWrapper>();
        for (Method method : featureClass.getMethods()) {
            Scenario scenarioAnnotation = method.getAnnotation(Scenario.class);
            Ignore ignoreAnnotation = method.getAnnotation(Ignore.class);
            if (scenarioAnnotation != null && ignoreAnnotation == null) {
                Tag tagAnnotation = method.getAnnotation(Tag.class);
                String scenarioName = extractScenarioName(method, scenarioAnnotation);
                if (tagAnnotation != null) {
                    cucumberScenarios.add(new CucumberScenarioWrapper(scenarioName, Arrays.asList(tagAnnotation.value())));
                } else {
                    cucumberScenarios.add(new CucumberScenarioWrapper(scenarioName));
                }
            }
        }
        return cucumberScenarios;
    }

    private String extractScenarioName(Method method, Scenario scenarioAnnotation) {
        return scenarioAnnotation.value().isEmpty() ? method.getName().replace("_", " ") : scenarioAnnotation.value();
    }

    private String extractFeatureName(Class featureClass) {
        Feature featureAnnotation = (Feature) featureClass.getAnnotation(Feature.class);
        return featureAnnotation != null ? featureAnnotation.value() : featureClass.getSimpleName() + ".feature";
    }

    private class CucumberScenarioWrapper {
        private final String scenarioName;
        private final List<String> tags;

        public CucumberScenarioWrapper(String scenarioName) {
            this.scenarioName = scenarioName;
            tags = new ArrayList<String>();
        }

        private CucumberScenarioWrapper(String scenarioName, List<String> tags) {
            this.scenarioName = scenarioName;
            this.tags = tags;
        }

        @Override
        public String toString() {
            String res = scenarioName;
            if (!tags.isEmpty()) {
                res += ", tags=" + tags;
            }
            return res;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CucumberScenarioWrapper)) return false;

            CucumberScenarioWrapper that = (CucumberScenarioWrapper) o;

            if (!scenarioName.equals(that.scenarioName)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return scenarioName.hashCode();
        }
    }

    private static class CucumberScenarioMethod extends FrameworkMethod {
        private final CucumberScenarioWrapper cucumberScenario;

        public CucumberScenarioMethod(CucumberScenarioWrapper cucumberScenario) {
            super(null);
            this.cucumberScenario = cucumberScenario;
        }

        @Override
        public String getName() {
            return cucumberScenario.scenarioName;
        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CucumberScenarioMethod)) return false;

            CucumberScenarioMethod that = (CucumberScenarioMethod) o;

            if (!cucumberScenario.equals(that.cucumberScenario)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = 31 * cucumberScenario.hashCode();
            return result;
        }

        public String tagsAsCSV() {
            String res = "";
            for (String tag : cucumberScenario.tags) {
                res += tag + ",";
            }
            if (res.endsWith(",")){
                res = res.substring(0,res.length() -1);
            }
            return res;
        }

        public boolean hasTags() {
            return !cucumberScenario.tags.isEmpty();
        }
    }
}
