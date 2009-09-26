package cuke4duke.internal.js;

import cuke4duke.internal.language.StepArgument;
import cuke4duke.internal.language.StepDefinition;
import org.jruby.RubyArray;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.regexp.NativeRegExp;

import java.util.List;

public class JsStepDefinition implements StepDefinition {
    private final Context cx;
    private final Scriptable scope;
    private final NativeObject jsStepDefinition;
    private final NativeFunction argumentsFrom;
    private final NativeRegExp regexp;
    private final NativeFunction closure;
    private List<StepArgument> arguments;

    public JsStepDefinition(Context cx, Scriptable scope, NativeObject jsStepDefinition, NativeFunction argumentsFrom, NativeRegExp regexp, NativeFunction closure) {
        this.cx = cx;
        this.scope = scope;
        this.jsStepDefinition = jsStepDefinition;
        this.argumentsFrom = argumentsFrom;
        this.regexp = regexp;
        this.closure = closure;
    }

    public String regexp_source() {
        return regexp.toString();
    }

    public String file_colon_line() {
        return jsStepDefinition.toString();
    }

    public void invoke(RubyArray rubyArgs) throws Throwable {
        Object[] args = rubyArgs.toArray();
        closure.call(cx, scope, scope, args);
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
