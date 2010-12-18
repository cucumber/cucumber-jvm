package cucumber.runtime.clojure;

import clojure.lang.AFunction;
import clojure.lang.RT;
import cucumber.runtime.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ClojureBackend implements Backend {
    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private String scriptPath;

    public ClojureBackend(String scriptPath) {
        this.scriptPath = scriptPath;
        try {
            defineStepDefinitions();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void defineStepDefinitions() throws Exception {
        RT.load("cucumber/runtime/clojure/dsl");
        Classpath.scan(this.scriptPath, ".rhino", new Consumer() {
            public void consume(Input input) throws IOException {
                try {
                    RT.load(input.getPath());
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
        });

    }

    public void addStepDefinition(Pattern regexp, AFunction body) {
        stepDefinitions.add(new ClojureStepDefinition(this, regexp, body));
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    public void newScenario() {
    }
}
