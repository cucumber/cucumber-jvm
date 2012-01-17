package cucumber.runtime;

import gherkin.formatter.model.Step;

import java.util.List;

public class StubBackend implements Backend {
    @Override
    public void buildWorld(List<String> gluePaths, World world) {
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(Step step) {
        return "SNIP";
    }
}
