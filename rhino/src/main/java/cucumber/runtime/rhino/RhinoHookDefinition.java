package cucumber.runtime.rhino;

import static java.util.Arrays.asList;
import cucumber.runtime.TagExpression;
import gherkin.pickles.PickleTag;

import java.util.Collection;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import cucumber.api.Scenario;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.Timeout;

public class RhinoHookDefinition implements HookDefinition {

    private Context cx;
    private Scriptable scope;
    private Function fn;
    private final TagExpression tagExpression;
    private final int order;
    private final long timeoutMillis;
    private StackTraceElement location;

    public RhinoHookDefinition(Context cx, Scriptable scope, Function fn, String[] tagExpressions, int order, long timeoutMillis, StackTraceElement location) {
        this.cx = cx;
        this.scope = scope;
        this.fn = fn;
        tagExpression = new TagExpression(asList(tagExpressions));
        this.order = order;
        this.timeoutMillis = timeoutMillis;
        this.location = location;
    }

    @Override
    public String getLocation(boolean detail) {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    @Override
    public void execute(Scenario scenario) throws Throwable {
        final Object[] args = new Object[] { scenario };
        Timeout.timeout(new Timeout.Callback<Object>() {
            @Override
            public Object call() throws Throwable {
                return fn.call(cx, scope, scope, args);
            }
        }, timeoutMillis);
    }

    @Override
    public boolean matches(Collection<PickleTag> tags) {
        return tagExpression.evaluate(tags);
    }

    @Override
    public int getOrder() {
        return order;
    }

    TagExpression getTagExpression() {
        return tagExpression;
    }

    long getTimeout() {
        return timeoutMillis;
    }

    @Override
    public boolean isScenarioScoped() {
        return false;
    }
}
