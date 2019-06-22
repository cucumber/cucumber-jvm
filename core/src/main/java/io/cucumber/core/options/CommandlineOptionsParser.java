package io.cucumber.core.options;

import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import io.cucumber.core.model.RerunLoader;

import java.util.Arrays;
import java.util.List;

public final class CommandlineOptionsParser {

    private final ResourceLoader resourceLoader;

    public CommandlineOptionsParser() {
        this(new MultiLoader(CommandlineOptionsParser.class.getClassLoader()));
    }

    public CommandlineOptionsParser(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public RuntimeOptionsBuilder parse(List<String> args) {
        RerunLoader rerunLoader = new RerunLoader(resourceLoader);
        RuntimeOptionsParser parser = new RuntimeOptionsParser(rerunLoader);
        return parser.parse(args);
    }

    public RuntimeOptionsBuilder parse(String... args) {
        return parse(Arrays.asList(args));
    }
}
