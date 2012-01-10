package cucumber.runtime.clojure;

import clojure.lang.AFunction;
import clojure.lang.RT;
import cucumber.resources.Consumer;
import cucumber.resources.Resource;
import cucumber.resources.Resources;
import cucumber.runtime.Backend;
import cucumber.runtime.CucumberException;
import cucumber.runtime.World;
import gherkin.formatter.model.Step;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class ClojureBackend implements Backend {
    private static ClojureBackend instance;
    private World world;

    public ClojureBackend() throws ClassNotFoundException, IOException {
        instance = this;
        RT.load("cucumber/runtime/clojure/dsl");
    }

    @Override
    public void buildWorld(List<String> gluePaths, World world) {
        this.world = world;
        for (String gluePath : gluePaths) {
            Resources.scan(gluePath.replace('.', '/'), ".clj", new Consumer() {
                public void consume(Resource resource) {
                    try {
                        RT.load(resource.getPath().replaceAll(".clj$", ""));
                    } catch (Exception e) {
                        throw new CucumberException("Failed to parse file " + resource.getPath(), e);
                    }
                }
            });
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
