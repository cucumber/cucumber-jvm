package io.cucumber.core.feature;

import io.cucumber.core.io.Resource;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;

final class FeatureBuilder {

    private final Logger log = LoggerFactory.getLogger(FeatureBuilder.class);
    private final Map<String, Map<String, CucumberFeature>> sourceToFeature = new HashMap<>();
    private final List<CucumberFeature> features = new ArrayList<>();

    public List<CucumberFeature> build() {
        List<CucumberFeature> cucumberFeatures = new ArrayList<>(features);
        cucumberFeatures.sort(comparing(CucumberFeature::getUri));
        return cucumberFeatures;
    }

    public void parse(Resource resource) {
        CucumberFeature parsedFeature = FeatureParser.parseResource(resource);
        String parsedFileName = getFileName(parsedFeature);

        Map<String, CucumberFeature> existingFeatures = sourceToFeature.get(parsedFeature.getSource());
        if (existingFeatures != null) {
            // Same contents but different file names was probably intentional
            CucumberFeature existingFeature = existingFeatures.get(parsedFileName);
            if (existingFeature != null) {
                log.error("" +
                    "Duplicate feature found: " +
                    parsedFeature.getUri() + " was identical to " + existingFeature.getUri() + "\n" +
                    "\n" +
                    "This typically happens when you configure cucumber to look " +
                    "for features in the root of your project.\nYour build tool " +
                    "creates a copy of these features in a 'target' or 'build'" +
                    "directory.\n" +
                    "\n" +
                    "If your features are on the class path consider using a class path URI.\n" +
                    "For example: 'classpath:com/example/app.feature'\n" +
                    "Otherwise you'll have to provide a more specific location"
                );
                return;
            }
        }
        sourceToFeature.putIfAbsent(parsedFeature.getSource(), new HashMap<>());
        sourceToFeature.get(parsedFeature.getSource()).put(parsedFileName, parsedFeature);
        features.add(parsedFeature);
    }

    private String getFileName(CucumberFeature feature) {
        String uri = feature.getUri().getSchemeSpecificPart();
        int i = uri.lastIndexOf("/");
        return i > 0 ? uri.substring(i) : uri;
    }
}
