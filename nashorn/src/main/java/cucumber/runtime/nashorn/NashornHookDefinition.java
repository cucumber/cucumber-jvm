package cucumber.runtime.nashorn;

import static java.util.Arrays.asList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.script.Bindings;
import javax.script.ScriptEngine;

import cucumber.api.Scenario;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.TagPredicate;
import cucumber.runtime.Timeout;
import gherkin.pickles.PickleTag;

public class NashornHookDefinition implements HookDefinition {
	private ScriptEngine engine;
	private Bindings engineScope;

    private Object fn;
    private final TagPredicate tagPredicate;
    private final int order;
    private final long timeoutMillis;
    private StackTraceElement location;

    public NashornHookDefinition(ScriptEngine engine, Bindings engineScope, Object fn, String[] tagExpressions, int order, long timeoutMillis, StackTraceElement location) {
    	this.engine = engine;
    	this.engineScope = engineScope;
        this.fn = fn;
        this.tagPredicate = new TagPredicate(asList(tagExpressions));
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
            	Method method = fn.getClass().getMethod("call",Object.class, Object[].class);
            	// pass the scope as the 'this' pointer (2nd argument)
            	try {
					return method.invoke(fn, engineScope, args);
            	} catch(RuntimeException e) {
            		throw e;
            	/*
            	 * i need to throw InterruptedException because Timeout.timeout() catches an InterruptedException explicitly
            	 */
            	} catch(InvocationTargetException e) {
            		if(e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof InterruptedException) {
            			throw e.getCause().getCause();
            		} else {
            			throw e;
            		}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
            }
        }, timeoutMillis);
    }

    TagPredicate getTagPredicate() {
        return tagPredicate;
    }

    @Override
    public boolean matches(Collection<PickleTag> tags) {
        return tagPredicate.apply(tags);
    }

    @Override
    public int getOrder() {
        return order;
    }

    long getTimeout() {
        return timeoutMillis;
    }

    @Override
    public boolean isScenarioScoped() {
        return false;
    }
}
