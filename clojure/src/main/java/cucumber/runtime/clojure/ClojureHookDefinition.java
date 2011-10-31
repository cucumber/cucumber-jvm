package cucumber.runtime.clojure;

// import cucumber.runtime.CucumberException;

import clojure.lang.AFunction;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.ScenarioResult;
import cucumber.runtime.Utils;
import gherkin.TagExpression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import static java.util.Arrays.asList;

public class ClojureHookDefinition implements HookDefinition {

    private final AFunction closure;
    private final int order;
    private final TagExpression tagExpression;

    public ClojureHookDefinition(String[] tagExpressions, AFunction closure) {
        tagExpression = new TagExpression(asList(tagExpressions));
        this.order = 0;
        this.closure = closure;
    }

    // Clojure's AFunction.invokeWithArgs doesn't take varargs :-/
    private Method lookupInvokeMethod(Object[] args) throws NoSuchMethodException {
        return AFunction.class.getMethod("invoke", (Class<?>[]) Utils.listOf(args.length, String.class).toArray()  );
    }

    @Override
    public void execute(ScenarioResult scenarioResult) throws Throwable {
        Object[] args = new Object[0];
        Method functionInvoke = lookupInvokeMethod(args);
        try {
            functionInvoke.invoke(closure, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    @Override
    public boolean matches(Collection<String> tags) {
        return tagExpression.eval(tags);
    }

    @Override
    public int getOrder() {
        return order;
    }

}
