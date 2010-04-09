package cuke4duke.internal.js;

import cuke4duke.internal.language.AbstractProgrammingLanguage;
import cuke4duke.internal.language.LanguageMixin;
import cuke4duke.spi.ExceptionFactory;
import org.jruby.runtime.builtin.IRubyObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.regexp.NativeRegExp;
import org.mozilla.javascript.tools.shell.Global;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JsLanguage extends AbstractProgrammingLanguage {
    private static final String JS_DSL = "/cuke4duke/internal/js/js_dsl.js";
    private final List<String> jsFiles = new ArrayList<String>();
    private Context cx;
    private Scriptable scope;

    public JsLanguage(LanguageMixin languageMixin, ExceptionFactory exceptionFactory) throws Exception {
        super(languageMixin, exceptionFactory);
    }

    public void load_code_file(String jsFile) throws Throwable {
        jsFiles.add(jsFile);
    }

    protected void begin_scenario(IRubyObject scenario) throws Throwable {
        clearHooksAndStepDefinitions();
        cx = Context.enter();
        scope = new Global(cx); // This gives us access to global functions like load()
        scope.put("jsLanguage", scope, this);
        cx.evaluateReader(scope, new InputStreamReader(getClass().getResourceAsStream(JS_DSL)), JS_DSL, 1, null);
        for (String jsFile : jsFiles) {
            cx.evaluateReader(scope, new FileReader(jsFile), jsFile, 1, null);
        }
    }

    public void addStepDefinition(Global jsStepDefinition, NativeFunction argumentsFrom, NativeRegExp regexp, NativeFunction closure) throws Throwable {
        addStepDefinition(new JsStepDefinition(this, cx, scope, jsStepDefinition, argumentsFrom, regexp, closure));
    }

    public void end_scenario() throws Throwable {
    }

    @Override
    protected Object customTransform(Object arg, Class<?> parameterType, Locale locale) {
        return null;
    }
}
