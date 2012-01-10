package cucumber.runtime.clojure;

import clojure.lang.AFunction;
import clojure.lang.RT;
import cucumber.io.Resource;
import cucumber.io.ResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.World;
import gherkin.formatter.model.Step;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class ClojureBackend implements Backend {
    private static ClojureBackend instance;
    private final ResourceLoader resourceLoader;
    private World world;

    public ClojureBackend(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        instance = this;
        loadScript("cucumber/runtime/clojure/dsl");
    }

    @Override
    public void buildWorld(List<String> gluePaths, World world) {
        this.world = world;
        for (String gluePath : gluePaths) {
            for (Resource resource : resourceLoader.resources(gluePath, ".clj")) {
                loadScript(resource.getPath());
            }
        }
    }

    private void loadScript(String path) {
        try {
            RT.load(path.replaceAll(".clj$", ""), true);
        } catch (IOException e) {
            throw new CucumberException(e);
        } catch (ClassNotFoundException e) {
            throw new CucumberException(e);
        }
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(Step step) {
        return new ClojureSnippetGenerator(step).getSnippet();
    }

    private StackTraceElement stepDefLocation(String interpreterClassName, String interpreterMethodName) {
        Throwable t = new Throwable();
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++) {
            StackTraceElement element = stackTraceElements[i];
            if (element.getClassName().equals(interpreterClassName) && element.getMethodName().equals(interpreterMethodName)) {
                return stackTraceElements[i - 1];
            }
        }
        throw new CucumberException("Couldn't find location for step definition");
    }

    public static void addStepDefinition(Pattern regexp, AFunction body) {
        StackTraceElement location = instance.stepDefLocation("clojure.lang.Compiler", "eval");
        instance.world.addStepDefinition(new ClojureStepDefinition(regexp, body, location));
    }

    public static void addBeforeHook(AFunction body) {
        instance.world.addBeforeHook(new ClojureHookDefinition(new String[0], body));
    }

    public static void addAfterHook(AFunction body) {
        instance.world.addAfterHook(new ClojureHookDefinition(new String[0], body));
    }
}
