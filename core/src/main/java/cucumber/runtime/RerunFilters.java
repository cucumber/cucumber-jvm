package cucumber.runtime;

import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.PathWithLines;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RerunFilters {
    private final RuntimeOptions runtimeOptions;
    private final ResourceLoader resourceLoader;

    public RerunFilters(RuntimeOptions runtimeOptions, ResourceLoader resourceLoader) {
        this.runtimeOptions = runtimeOptions;
        this.resourceLoader = resourceLoader;
    }

    public Map<String, List<Long>> processRerunFiles() {
        final Map<String, List<Long>> lineFilters = new HashMap<String, List<Long>>();
        for (String featurePath : runtimeOptions.getFeaturePaths()) {
            if (featurePath.startsWith("@")) {
                for (PathWithLines pathWithLines : CucumberFeature.loadRerunFile(resourceLoader, featurePath.substring(1))) {
                    addLineFilters(lineFilters, pathWithLines.path, pathWithLines.lines);
                }
            }
        }
        return lineFilters;
    }

    private void addLineFilters(Map<String, List<Long>> parsedLineFilters, String key, List<Long> lines) {
        if (parsedLineFilters.containsKey(key)) {
            parsedLineFilters.get(key).addAll(lines);
        } else {
            parsedLineFilters.put(key, lines);
        }
    }


}
