package cucumber.runtime.filter;

import io.cucumber.core.options.FeatureOptions;
import cucumber.runtime.model.FeatureLoader;
import cucumber.runtime.model.PathWithLines;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RerunFilters {
    private final FeatureOptions featureOptions;
    private final FeatureLoader featureLoader;

    public RerunFilters(FeatureOptions featureOptions, FeatureLoader featureLoader) {
        this.featureOptions = featureOptions;
        this.featureLoader = featureLoader;
    }

    Map<String, List<Long>> processRerunFiles() {
        final Map<String, List<Long>> lineFilters = new HashMap<String, List<Long>>();
        for (String featurePath : featureOptions.getFeaturePaths()) {
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
