package cucumber.runner;

import cucumber.runtime.Backend;
import io.cucumber.core.options.RuntimeOptions;
import cucumber.runtime.Glue;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.pickles.PickleStep;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

public class TestRunnerSupplier implements Backend, RunnerSupplier {

    private final EventBus bus;
    private final RuntimeOptions runtimeOptions;

    protected TestRunnerSupplier(EventBus bus, RuntimeOptions runtimeOptions) {
        this.bus = bus;
        this.runtimeOptions = runtimeOptions;
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {

    }

    @Override
    public void buildWorld() {

    }

    @Override
    public void disposeWorld() {

    }

    @Override
    public List<String> getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        return emptyList();
    }

    @Override
    public Runner get() {
        return new Runner(bus, Collections.singleton(this), runtimeOptions);
    }
}
