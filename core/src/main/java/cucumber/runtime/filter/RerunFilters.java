package cucumber.runtime.filter;

import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.model.FeatureLoader;
import cucumber.runtime.model.PathWithLines;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RerunFilters {
    private final RuntimeOptions runtimeOptions;
    private final FeatureLoader featureLoader;

    public RerunFilters(RuntimeOptions runtimeOptions, FeatureLoader featureLoader) {
        this.runtimeOptions = runtimeOptions;
        this.featureLoader = featureLoader;
    }


    Map<String, List<Long>> processRerunFiles() {
        final Map<String, List<Long>> lineFilters = new HashMap<String, List<Long>>();
        for (String featurePath : runtimeOptions.getFeaturePaths()) {
            if (featurePath.startsWith("@")) {
                for (PathWithLines pathWithLines : featureLoader.loadRerunFile(featurePath.substring(1))) {
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
