package cucumber.cli;

import cucumber.io.ResourceLoader;
import cucumber.runtime.Runtime;

import java.util.List;

public interface RuntimeFactory {
    Runtime createRuntime(ResourceLoader fileResourceLoader, List<String> gluePaths, ClassLoader classLoader, boolean dryRun);
}
