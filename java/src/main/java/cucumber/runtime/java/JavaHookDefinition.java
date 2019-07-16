package cucumber.runtime.java;

import cucumber.api.Scenario;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.CucumberException;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.MethodFormat;
import cucumber.runtime.Utils;
import cucumber.runtime.filter.TagPredicate;
import gherkin.pickles.PickleTag;
import io.cucumber.core.event.Status;

import java.lang.reflect.Method;
import java.util.Collection;

import static java.util.Arrays.asList;

public class JavaHookDefinition implements HookDefinition {

    private final Method method;
    private final long timeoutMillis;
    private final TagPredicate tagPredicate;
    private final int order;
    private final ObjectFactory objectFactory;

    JavaHookDefinition(Method method, String[] tagExpressions, int order, long timeoutMillis, ObjectFactory objectFactory) {
        this.method = method;
        this.timeoutMillis = timeoutMillis;
        this.tagPredicate = new TagPredicate(asList(tagExpressions));
        this.order = order;
        this.objectFactory = objectFactory;
    }

    Method getMethod() {
        return method;
    }

    @Override
    public String getLocation(boolean detail) {
        MethodFormat format = detail ? MethodFormat.FULL : MethodFormat.SHORT;
        return format.format(method);
    }

    @Override
    public void execute(Scenario scenario) throws Throwable {
        Object[] args;
        switch (method.getParameterTypes().length) {
            case 0:
                args = new Object[0];
                break;
            case 1:
                Class<?> parameterType = method.getParameterTypes()[0];
                if(Scenario.class.equals(parameterType)) {
                    args = new Object[]{scenario};
                } else if(io.cucumber.core.api.Scenario.class.equals(parameterType)){
                    args = new Object[]{new ScenarioAdaptor(scenario)};
                } else {
                    throw new CucumberException("When a hook declares an argument it must be of type " + io.cucumber.core.api.Scenario.class.getName() + ". " + method.toString());
                }
                break;
            default:
                throw new CucumberException("Hooks must declare 0 or 1 arguments. " + method.toString());
        }

        Utils.invoke(objectFactory.getInstance(method.getDeclaringClass()), method, timeoutMillis, args);
    }

    @Override
    public boolean matches(Collection<PickleTag> tags) {
        return tagPredicate.apply(tags);
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public boolean isScenarioScoped() {
        return false;
    }

    private static class ScenarioAdaptor implements io.cucumber.core.api.Scenario {
        private final Scenario scenario;

        ScenarioAdaptor(Scenario scenario) {
            this.scenario = scenario;
        }

        @Override
        public Collection<String> getSourceTagNames() {
            return scenario.getSourceTagNames();
        }

        @Override
        public Status getStatus() {
            return Status.valueOf(scenario.getStatus().name());
        }

        @Override
        public boolean isFailed() {
            return scenario.isFailed();
        }

        @Override
        public void embed(byte[] data, String mimeType) {
            scenario.embed(data, mimeType);
        }

        @Override
        public void embed(byte[] data, String mimeType, String name) {
            scenario.embed(data, mimeType, name);
        }

        @Override
        public void write(String text) {
            scenario.write(text);
        }

        @Override
        public String getName() {
            return scenario.getName();
        }

        @Override
        public String getId() {
            return scenario.getId();
        }

        @Override
        public String getUri() {
            return scenario.getUri();
        }

        @Override
        public Integer getLine() {
            return scenario.getLines().get(0);
        }
    }
}
