package cuke4duke.internal.js;

import cuke4duke.internal.language.ProgrammingLanguage;
import cuke4duke.internal.language.LanguageMixin;

import org.mozilla.javascript.regexp.NativeRegExp;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.NativeFunction;

import java.io.FileReader;
import java.io.InputStreamReader;

public class JsLanguage extends ProgrammingLanguage {
    private static final String JS_DSL = "/cuke4duke/internal/js/js_dsl.js";
    private Context cx;
    private Scriptable scope;

    public JsLanguage(LanguageMixin languageMixin) throws Exception {
        super(languageMixin);
        cx = Context.enter();
        scope = cx.initStandardObjects();
        scope.put("jsLanguage", scope, this);

//        instance = this;
        cx.evaluateReader(scope, new InputStreamReader(getClass().getResourceAsStream(JS_DSL)), JS_DSL, 1, null);

    }

    public void addStepDefinition(NativeRegExp regexp, NativeFunction closure) throws Exception {
        System.out.println("CLOSURE:" + closure+ ":" + closure.getClass().getSuperclass());
        //instance.createAndAddStepDefinition(regexp, closure);
    }

    public void begin_scenario() {
    }

    public void end_scenario() {
    }

    protected void load(String file) throws Throwable {
        cx.evaluateReader(scope, new FileReader(file), file, 1, null);
    }
}
