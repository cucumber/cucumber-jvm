package cucumber.runtime;

import cucumber.runner.Runner;

public interface RunnerSupplier {
    Runner get();
}
