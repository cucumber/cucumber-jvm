package cucumber.runtime.rhino;

import cucumber.classpath.Classpath;
import cucumber.classpath.Consumer;
import cucumber.io.Resource;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.javascript.JavascriptSnippetGenerator;
import gherkin.formatter.model.Step;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        Classpath.scan(this.scriptPath, ".js", new Consumer() {
            public void consume(Resource resource) {
                try {
                    cx.evaluateReader(scope, resource.getReader(), resource.getPath(), 1, null);
                } catch (IOException e) {
                    throw new CucumberException("Failed to evaluate Javascript in " + resource.getPath(), e);
                }
            }
        });
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    public void newWorld() {
    }

    public void disposeWorld() {
    }

    public String getSnippet(Step step) {
        return new JavascriptSnippetGenerator(step).getSnippet();
    }

    private StackTraceElement stepDefLocation(String extension) {
        Throwable t = new Throwable();
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            boolean js = stackTraceElement.getFileName().endsWith(extension);
            boolean inScriptPath = stackTraceElement.getFileName().startsWith(scriptPath);
            boolean hasLine = stackTraceElement.getLineNumber() != -1;
            if (js && inScriptPath && hasLine) {
                return stackTraceElement;
            }
        }
        throw new RuntimeException("Couldn't find location for step definition");
    }

    public void addStepDefinition(Global jsStepDefinition, NativeFunction bodyFunc, NativeFunction argumentsFromFunc, Locale locale) throws Throwable {
        StackTraceElement stepDefLocation = stepDefLocation(".js");
        RhinoStepDefinition stepDefinition = new RhinoStepDefinition(cx, scope, jsStepDefinition, bodyFunc, stepDefLocation, argumentsFromFunc, locale);
        stepDefinitions.add(stepDefinition);
    }
}
