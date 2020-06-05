package io.cucumber.core.feature;

import java.net.URI;
import java.nio.file.Path;

/**
 * Identifies a single feature.
 * <p>
 * Features are identified by a URI as defined in {@link FeaturePath}.
 * Additionally the scheme specific part must end with {@code .feature}
 *
 * @see FeatureWithLines
 */
public class FeatureIdentifier {

    private static final String FEATURE_FILE_SUFFIX = ".feature";

    private FeatureIdentifier() {

    }

    public static URI parse(String featureIdentifier) {
        return parse(FeaturePath.parse(featureIdentifier));
    }

    public static URI parse(URI featureIdentifier) {
        if (!isFeature(featureIdentifier)) {
            throw new IllegalArgumentException(
                "featureIdentifier does not reference a single feature file: " + featureIdentifier);
        }
        return featureIdentifier;
    }

    public static boolean isFeature(URI featureIdentifier) {
        return featureIdentifier.getSchemeSpecificPart().endsWith(FEATURE_FILE_SUFFIX);
    }

    public static boolean isFeature(Path path) {
        return path.getFileName().toString().endsWith(FEATURE_FILE_SUFFIX);
    }

}
