package cucumber.runtime.model;

import cucumber.runtime.io.Resource;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureBuilder {

    private final Logger log = LoggerFactory.getLogger(FeatureBuilder.class);
    private final Map<String, CucumberFeature> sourceToFeature = new HashMap<>();

    public List<CucumberFeature> build() {
        List<CucumberFeature> cucumberFeatures = new ArrayList<>(sourceToFeature.values());
        Collections.sort(cucumberFeatures, new CucumberFeature.CucumberFeatureUriComparator());
        return cucumberFeatures;
    }

    public void parse(Resource resource) {
        CucumberFeature parsedFeature = FeatureParser.parseResource(resource);
        CucumberFeature existingFeature = sourceToFeature.get(parsedFeature.getSource());
        if (existingFeature != null) {
            log.warn("Duplicate feature ignored. " + parsedFeature.getUri() + " was identical to " + existingFeature.getUri());
            return;
        }
        sourceToFeature.put(parsedFeature.getSource(), parsedFeature);
    }
}
