package cucumber.runtime.rhino;

import cucumber.runtime.StepDefinition;
import cucumber.runtime.Utils;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.regexp.NativeRegExp;
import org.mozilla.javascript.tools.shell.Global;

import java.lang.reflect.Type;
import java.util.List;

public class RhinoStepDefinition implements StepDefinition {
    private final Context cx;
    private final Scriptable scope;
    private final Global jsStepDefinition;
    private final NativeRegExp regexp;
    private final NativeFunction bodyFunc;
    private final StackTraceElement location;
    private final NativeFunction argumentsFromFunc;

    public RhinoStepDefinition(Context cx, Scriptable scope, Global jsStepDefinition, NativeRegExp regexp, NativeFunction bodyFunc, StackTraceElement location, NativeFunction argumentsFromFunc) {
        this.cx = cx;
        this.scope = scope;
        this.jsStepDefinition = jsStepDefinition;
        this.regexp = regexp;
        this.bodyFunc = bodyFunc;
        this.location = location;
        this.argumentsFromFunc = argumentsFromFunc;
    }

    public List<Argument> matchedArguments(Step step) {
        NativeJavaObject args = (NativeJavaObject) argumentsFromFunc.call(cx, scope, jsStepDefinition, new Object[]{step.getName(), this});
        return args == null ? null : (List<Argument>) args.unwrap();
    }

    @Override
    public Type getTypeForTableList(int argIndex) {
        return null;
    }

    public String getLocation() {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    public Class<?>[] getParameterTypes() {
        return Utils.classArray(bodyFunc.getArity(), String.class);
    }

    public void execute(Object[] args) throws Throwable {
        bodyFunc.call(cx, scope, scope, args);
    }

    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return location.getFileName().equals(stackTraceElement.getFileName());
    }

    @Override
    public String getPattern() {
        return regexp.toString();
    }
}
