package cucumber.runtime.clojure;

import clojure.lang.IFn;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.ScenarioResult;
import cucumber.runtime.Utils;
import gherkin.TagExpression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import static java.util.Arrays.asList;

public class ClojureHookDefinition implements HookDefinition {

    private final TagExpression tagExpression;
    private final IFn closure;

    public ClojureHookDefinition(String[] tagExpressions, IFn closure) {
        tagExpression = new TagExpression(asList(tagExpressions));
        this.closure = closure;
    }

    // Clojure's AFunction.invokeWithArgs doesn't take varargs :-/
    private Method lookupInvokeMethod(Object[] args) throws NoSuchMethodException {
        return IFn.class.getMethod("invoke", (Class<?>[]) Utils.listOf(args.length, String.class).toArray());
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
        return 0;
    }

}
