package cuke4duke.internal.js;

import cuke4duke.internal.language.AbstractStepDefinition;
import cuke4duke.internal.language.StepArgument;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.regexp.NativeRegExp;
import org.mozilla.javascript.tools.shell.Global;

import java.util.List;

public class JsStepDefinition extends AbstractStepDefinition {
    private final Context cx;
    private final Scriptable scope;
    private final Global jsStepDefinition;
    private final NativeFunction argumentsFrom;
    private final NativeRegExp regexp;
    private final NativeFunction closure;
    private List<StepArgument> arguments;

    public JsStepDefinition(JsLanguage programmingLanguage, Context cx, Scriptable scope, Global jsStepDefinition, NativeFunction argumentsFrom, NativeRegExp regexp, NativeFunction closure) throws Throwable {
        super(programmingLanguage);
        this.cx = cx;
        this.scope = scope;
        this.jsStepDefinition = jsStepDefinition;
        this.argumentsFrom = argumentsFrom;
        this.regexp = regexp;
        this.closure = closure;
        register();
    }

    public String regexp_source() {
        return regexp.toString();
    }

    public String file_colon_line() {
        return regexp_source();
    }

    public Object invokeWithArgs(Object[] args) throws Throwable {
        return closure.call(cx, scope, scope, args);
    }

    public List<StepArgument> arguments_from(String stepName) {
        arguments = null;
        argumentsFrom.call(cx, scope, jsStepDefinition, new Object[]{stepName, this});
        return arguments;
    }

    public void addArguments(List<StepArgument> arguments) {
        this.arguments = arguments;
    }
}
