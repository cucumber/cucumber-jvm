package cucumber.runner;

import cucumber.runtime.Backend;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.pickles.PickleStep;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

public abstract class TestBackendSupplier implements Backend, BackendSupplier {

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
    public Collection<? extends Backend> get() {
        return  Collections.singleton(this);
    }
}
