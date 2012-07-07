package cucumber.runtime.rhino;

import cucumber.runtime.ParameterType;
import cucumber.runtime.StepDefinition;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
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

    public String getLocation(boolean detail) {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    @Override
    public Integer getParameterCount() {
        return bodyFunc.getArity();
    }

    @Override
    public ParameterType getParameterType(int n, Type argumentType) {
        return new ParameterType(argumentType, null, null);
    }

    public void execute(I18n i18n, Object[] args) throws Throwable {
        try {
            bodyFunc.call(cx, scope, scope, args);
        } catch (JavaScriptException e) {
            Object value = e.getValue();
            if (value instanceof NativeJavaObject) {
                NativeJavaObject njo = (NativeJavaObject) value;
                Object unwrapped = njo.unwrap();
                if (unwrapped instanceof Throwable) {
                    throw (Throwable) unwrapped;
                }
            }
            throw e.getCause() == null ? e : e.getCause();
        }
    }

    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return location.getFileName().equals(stackTraceElement.getFileName());
    }

    @Override
    public String getPattern() {
        return regexp.toString();
    }
}
