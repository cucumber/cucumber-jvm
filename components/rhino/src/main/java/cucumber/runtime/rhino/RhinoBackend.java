package cucumber.runtime.rhino;

import cucumber.runtime.*;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.regexp.NativeRegExp;
import org.mozilla.javascript.tools.shell.Global;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RhinoBackend implements Backend {
    private static final String JS_DSL = "/cucumber/runtime/rhino/dsl.js";
    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private Context cx;
    private Scriptable scope;
    private final String scriptPath;

    public RhinoBackend(String scriptPath) {
        this.scriptPath = scriptPath;
        try {
            defineStepDefinitions();
        } catch (IOException e) {
            throw new CucumberException("Couldn't load stepdefs", e);
        }
    }

    private void defineStepDefinitions() throws IOException {
        cx = Context.enter();
        scope = new Global(cx); // This gives us access to global functions like load()
        scope.put("jsBackend", scope, this);
        InputStreamReader dsl = new InputStreamReader(getClass().getResourceAsStream(JS_DSL));
        cx.evaluateReader(scope, dsl, JS_DSL, 1, null);

        Classpath.scan(this.scriptPath, ".rhino", new Consumer() {
            public void consume(Input input) throws IOException {
                cx.evaluateReader(scope, input.getReader(), input.getPath(), 1, null);
            }
        });
    }

    public void addStepDefinition(Global jsStepDefinition, NativeRegExp regexp, NativeFunction bodyFunc, NativeFunction argumentsFromFunc) throws Throwable {
        StackTraceElement stepDefLocation = stepDefLocation();
        RhinoStepDefinition stepDefinition = new RhinoStepDefinition(cx, scope, jsStepDefinition, regexp, bodyFunc, stepDefLocation, argumentsFromFunc);
        stepDefinitions.add(stepDefinition);
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    public void newScenario() {
    }

    private StackTraceElement stepDefLocation() {
        Throwable t = new Throwable();
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            boolean js = stackTraceElement.getFileName().endsWith(".rhino");
            boolean inScriptPath = stackTraceElement.getFileName().startsWith(scriptPath);
            boolean hasLine = stackTraceElement.getLineNumber() != -1;
            if(js && inScriptPath && hasLine) {
                return stackTraceElement;
            }
        }
        throw new RuntimeException("Couldn't find location for step definition");
    }
}
