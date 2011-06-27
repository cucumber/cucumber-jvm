package cucumber.runtime.clojure;

import clojure.lang.AFunction;
import clojure.lang.RT;
import cucumber.classpath.Classpath;
import cucumber.classpath.Consumer;
import cucumber.classpath.Input;
import cucumber.runtime.*;
import gherkin.formatter.model.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ClojureBackend implements Backend {
    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private String scriptPath;
    private static ClojureBackend instance;

    public ClojureBackend(String scriptPath) {
        instance = this;
        this.scriptPath = scriptPath;
        try {
            defineStepDefinitions();
        } catch (Exception e) {
            throw new CucumberException("Failed to define Cloure Step Definitions", e);
        }
    }

    private void defineStepDefinitions() throws Exception {
        RT.load("cucumber/runtime/clojure/dsl");
        Classpath.scan(this.scriptPath, ".clj", new Consumer() {
            public void consume(Input input) {
                try {
                    RT.load(input.getPath().replaceAll(".clj$", ""));
                } catch (Exception e) {
                    throw new CucumberException("Failed to parse file " + input.getPath(), e);
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
        instance.stepDefinitions.add(new ClojureStepDefinition(regexp, body, location));
    }

}
