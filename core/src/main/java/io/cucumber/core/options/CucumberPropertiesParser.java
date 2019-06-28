package io.cucumber.core.options;

import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.feature.RerunLoader;

import java.util.List;
import java.util.Map;

public final class CucumberPropertiesParser {

    private final ResourceLoader resourceLoader;

    public CucumberPropertiesParser(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public CucumberPropertiesParser() {
        this(new MultiLoader(CucumberPropertiesParser.class.getClassLoader()));
    }

    public RuntimeOptionsBuilder parse(Map<String, String> properties){
        String cucumberOptions = properties.get("cucumber.options");
        if (cucumberOptions == null) {
            return new RuntimeOptionsBuilder();
        }

        RerunLoader rerunLoader = new RerunLoader(resourceLoader);
        RuntimeOptionsParser parser = new RuntimeOptionsParser(rerunLoader);
        List<String> args = ShellWords.parse(cucumberOptions);
        return parser.parse(args);
    }

}
