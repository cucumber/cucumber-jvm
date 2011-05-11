package cucumber.runtime.rhino;

import cucumber.runtime.StepDefinition;
import gherkin.formatter.Argument;
import gherkin.model.Step;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;

import java.util.List;

public class RhinoStepDefinition implements StepDefinition {
    private final Context cx;
    private final Scriptable scope;
    private final Global jsStepDefinition;
    private final NativeFunction bodyFunc;
    private final StackTraceElement location;
    private final NativeFunction argumentsFromFunc;

    public RhinoStepDefinition(Context cx, Scriptable scope, Global jsStepDefinition, NativeFunction bodyFunc, StackTraceElement location, NativeFunction argumentsFromFunc) {
        this.cx = cx;
        this.scope = scope;
        this.jsStepDefinition = jsStepDefinition;
        this.bodyFunc = bodyFunc;
        this.location = location;
        this.argumentsFromFunc = argumentsFromFunc;
    }

    public List<Argument> matchedArguments(Step step) {
        NativeJavaObject args = (NativeJavaObject) argumentsFromFunc.call(cx, scope, jsStepDefinition, new Object[]{step.getName(), this});
        return args == null ? null : (List<Argument>) args.unwrap();
    }

    public String getLocation() {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    public Class<?>[] getParameterTypes() {
        Class[] types = new Class[bodyFunc.getArity()];
        for (int i = 0; i < types.length; i++) {
            types[i] = String.class;
        }
        return types;
    }

    public void execute(Object[] args) throws Throwable {
        bodyFunc.call(cx, scope, scope, args);
    }

    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return location.getFileName().equals(stackTraceElement.getFileName());
    }
}
