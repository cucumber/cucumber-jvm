package io.cucumber.core.options;

import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.model.RerunLoader;

import java.util.Collections;
import java.util.List;

public class EnvironmentOptionsParser {

    private final ResourceLoader resourceLoader;


    public EnvironmentOptionsParser(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public EnvironmentOptionsParser() {
        this(new MultiLoader(EnvironmentOptionsParser.class.getClassLoader()));
    }

    public RuntimeOptionsBuilder parse(Env env){
        RerunLoader rerunLoader = new RerunLoader(resourceLoader);
        RuntimeOptionsParser parser = new RuntimeOptionsParser(rerunLoader);

        String cucumberOptionsFromEnv = env.get("cucumber.options");
        List<String> shellWords = Collections.emptyList();
        if (cucumberOptionsFromEnv != null) {
            shellWords = ShellWords.parse(cucumberOptionsFromEnv);
        }
        return parser.parse(shellWords);
    }

}
