package io.cucumber.junit;

import io.cucumber.core.runner.Runner;
import io.cucumber.core.runtime.RunnerSupplier;

class StubRunnerSupplier implements RunnerSupplier {
    @Override
    public Runner get() {
        return null;
    }
}
