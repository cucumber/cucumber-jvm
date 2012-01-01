package cucumber.runtime.rhino;

import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.World;
import cucumber.runtime.javascript.JavascriptSnippetGenerator;
import gherkin.formatter.model.Step;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.regexp.NativeRegExp;
import org.mozilla.javascript.tools.shell.Global;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class RhinoBackend implements Backend {
    private static final String JS_DSL = "/cucumber/runtime/rhino/dsl.js";
    private final Context cx;
    private final Scriptable scope;
    private List<String> gluePaths;
    private World world;

    public RhinoBackend() throws IOException {
        cx = Context.enter();
        scope = new Global(cx); // This gives us access to global functions like load()
        scope.put("jsBackend", scope, this);
        InputStreamReader dsl = new InputStreamReader(getClass().getResourceAsStream(JS_DSL));
        cx.evaluateReader(scope, dsl, JS_DSL, 1, null);
    }

    @Override
    public void buildWorld(List<String> gluePaths, World world) {
        this.gluePaths = gluePaths;
        this.world = world;

        for (String gluePath : gluePaths) {
            Iterable<Resource> resources = new ResourceLoader().fileResources(gluePath, ".js");
            for (Resource resource : resources) {
                try {
                    cx.evaluateReader(scope, new InputStreamReader(resource.getInputStream()), resource.getPath(), 1, null);
                } catch (IOException e) {
                    throw new CucumberException("Failed to evaluate Javascript in " + resource.getPath(), e);
                }
            }
        }
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(Step step) {
        return new JavascriptSnippetGenerator(step).getSnippet();
    }

    private StackTraceElement stepDefLocation(String extension) {
        Throwable t = new Throwable();
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            boolean js = stackTraceElement.getFileName().endsWith(extension);
            for (String scriptPath : gluePaths) {
                boolean inScriptPath = stackTraceElement.getFileName().startsWith(scriptPath);
                boolean hasLine = stackTraceElement.getLineNumber() != -1;
                if (js && inScriptPath && hasLine) {
                    return stackTraceElement;
                }
            }
        }
        throw new RuntimeException("Couldn't find location for step definition");
    }

    public void addStepDefinition(Global jsStepDefinition, NativeRegExp regexp, NativeFunction bodyFunc, NativeFunction argumentsFromFunc) throws Throwable {
        StackTraceElement stepDefLocation = stepDefLocation(".js");
        RhinoStepDefinition stepDefinition = new RhinoStepDefinition(cx, scope, jsStepDefinition, regexp, bodyFunc, stepDefLocation, argumentsFromFunc);
        world.addStepDefinition(stepDefinition);
    }
}
