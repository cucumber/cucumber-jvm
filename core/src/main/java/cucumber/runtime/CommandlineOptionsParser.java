package cucumber.runtime;

import cucumber.runtime.io.ResourceLoader;
import io.cucumber.core.model.RerunLoader;

import java.util.List;

public class CommandlineOptionsParser {

    private final ResourceLoader resourceLoader;

    public CommandlineOptionsParser(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public RuntimeOptionsParser.ParsedOptions parse(List<String> args) {
        RerunLoader rerunLoader = new RerunLoader(resourceLoader);
        RuntimeOptionsParser parser = new RuntimeOptionsParser(rerunLoader);
        return parser.parse(args);
    }
}
