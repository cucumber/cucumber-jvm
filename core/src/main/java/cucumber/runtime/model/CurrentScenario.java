package cucumber.runtime.model;

import gherkin.formatter.model.Scenario;

public class CurrentScenario {

    private static final ThreadLocal<Scenario> threadLocal = new ThreadLocal<Scenario>();

    public static Scenario get() {
        return threadLocal.get();
    }

    public static void set(Scenario scenario) {
        threadLocal.set(scenario);
    }
}
