package cucumber.runtime.rhino;

import static cucumber.runtime.io.MultiLoader.packageName;
import gherkin.formatter.model.Step;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.regexp.NativeRegExp;
import org.mozilla.javascript.tools.shell.Global;

import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameSanitizer;
import cucumber.runtime.snippets.SnippetGenerator;

public class RhinoBackend implements Backend {
    private static final String JS_DSL = "/cucumber/runtime/rhino/dsl.js";
    private final SnippetGenerator snippetGenerator = new SnippetGenerator(new JavaScriptSnippet());
    private final ResourceLoader resourceLoader;
    private final Context cx;
    private final Scriptable scope;
    private List<String> gluePaths;
    private Glue glue;
    private Function worldFunction;

    public RhinoBackend(ResourceLoader resourceLoader) throws IOException {
        this.resourceLoader = resourceLoader;
        cx = Context.enter();
        scope = new Global(cx); // This gives us access to global functions like load()
        scope.put("jsBackend", scope, this);
        InputStreamReader dsl = new InputStreamReader(getClass().getResourceAsStream(JS_DSL), "UTF-8");
        cx.evaluateReader(scope, dsl, JS_DSL, 1, null);
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        this.glue = glue;
        this.gluePaths = gluePaths;
        for (String gluePath : gluePaths) {
            for (Resource resource : resourceLoader.resources(gluePath, ".js")) {
                try {
                    cx.evaluateReader(scope, new InputStreamReader(resource.getInputStream(), "UTF-8"), resource.getPath(), 1, null);
                } catch (IOException e) {
                    throw new CucumberException("Failed to evaluate Javascript in " + resource.getPath(), e);
                }
            }
        }
    }

    @Override
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
        // Not used yet
    }

    @Override
    public void buildWorld() {
        if (worldFunction != null) worldFunction.call(cx, scope, scope, new Object[0]);
    }

    @Override
    public void disposeWorld() {
        worldFunction = null;
    }
    
    public void registerWorld(Function fn) {
        if (worldFunction != null) throw new CucumberException("World is already set");
        worldFunction = fn;
    }

    @Override
    public String getSnippet(Step step, FunctionNameSanitizer functionNameSanitizer) {
        return snippetGenerator.getSnippet(step, functionNameSanitizer);
    }

    private StackTraceElement jsLocation() {
        Throwable t = new Throwable();
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            boolean js = stackTraceElement.getFileName().endsWith(".js");
            for (String gluePath : gluePaths) {
                boolean inScriptPath = packageName(stackTraceElement.getFileName()).startsWith(packageName(gluePath));
                boolean hasLine = stackTraceElement.getLineNumber() != -1;
                if (js && inScriptPath && hasLine) {
                    return stackTraceElement;
                }
            }
        }
        throw new RuntimeException("Couldn't find location for step definition");
    }

    public void addStepDefinition(Global jsStepDefinition, NativeRegExp regexp, NativeFunction bodyFunc, NativeFunction argumentsFromFunc) throws Throwable {
        StackTraceElement stepDefLocation = jsLocation();
        RhinoStepDefinition stepDefinition = new RhinoStepDefinition(cx, scope, jsStepDefinition, regexp, bodyFunc, stepDefLocation, argumentsFromFunc);
        glue.addStepDefinition(stepDefinition);
    }

    public void addBeforeHook(Function fn, String[] tags, int order, int timeout) {
        StackTraceElement stepDefLocation = jsLocation();
        RhinoHookDefinition hookDefinition = new RhinoHookDefinition(cx, scope, fn, tags, order, timeout, stepDefLocation);
        glue.addBeforeHook(hookDefinition);
    }

    public void addAfterHook(Function fn, String[] tags, int order, int timeout) {
        StackTraceElement stepDefLocation = jsLocation();
        RhinoHookDefinition hookDefinition = new RhinoHookDefinition(cx, scope, fn, tags, order, timeout, stepDefLocation);
        glue.addAfterHook(hookDefinition);
    }
}
