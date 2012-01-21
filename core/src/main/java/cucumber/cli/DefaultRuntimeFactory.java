package cucumber.cli;

import cucumber.io.ResourceLoader;
import cucumber.runtime.Runtime;

import java.util.List;

public class DefaultRuntimeFactory implements RuntimeFactory {
    @Override
    public cucumber.runtime.Runtime createRuntime(ResourceLoader resourceLoader, List<String> gluePaths, ClassLoader classLoader, boolean dryRun) {
        return new Runtime(resourceLoader, gluePaths, classLoader, dryRun);
    }
}
