package cucumber.runner;

import cucumber.runner.EventBus;
import cucumber.runtime.Backend;
import cucumber.runtime.Glue;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.pickles.PickleStep;

import java.util.Collections;
import java.util.List;

public class TestRunnerSupplier implements Backend, RunnerSupplier {

    private final EventBus bus;
    private final RuntimeOptions runtimeOptions;

    protected TestRunnerSupplier(EventBus bus, RuntimeOptions runtimeOptions) {
        this.bus = bus;
        this.runtimeOptions = runtimeOptions;
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {

    }

    @Override
    public void buildWorld() {

    }

    @Override
    public void disposeWorld() {

    }

    @Override
    public String getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        return null;
    }

    @Override
    public Runner get() {
        return new Runner(bus, Collections.singleton(this), runtimeOptions);
    }
}
