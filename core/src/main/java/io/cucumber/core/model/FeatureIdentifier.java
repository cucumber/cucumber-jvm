package io.cucumber.core.model;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Identifies a feature.
 * <p>
 * Features are identified by a URI. This URI can either be absolute:
 * {@code scheme:/absolute/path/to.feature}, or relative to the
 * current working directory: {@code scheme:relative/path/to.feature}.
 * <p>
 * In either form, when the scheme is omitted {@code file} will be
 * assumed: {@code path/to.feature}.
 * <p>
 * <p>
 * On systems that use a {@code File.separatorChar} other then `{@code /}`
 * {@code File.separatorChar} can be used as a path separator. When
 * doing so when the scheme must be omitted: {@code path\to.feature}.
 * <em>It is recommended to use `{@code /}` as the path separator.</em>
 *
 * @see FeatureWithLines
 */
public class FeatureIdentifier {

    private FeatureIdentifier() {

    }

    public static URI parse(String featureIdentifier) {
        if (nonStandardPathSeparatorInUse(featureIdentifier)) {
            String standardized = replaceNonStandardPathSeparator(featureIdentifier);
            return parseAssumeFileScheme(standardized);
        }

        if (probablyURI(featureIdentifier)) {
            return parseProbableURI(featureIdentifier);
        }

        return parseAssumeFileScheme(featureIdentifier);
    }

    private static URI parseProbableURI(String featureIdentifier) {
        return URI.create(featureIdentifier);
    }

    private static boolean probablyURI(String featureIdentifier) {
        return featureIdentifier.matches("^\\w+:.*$");
    }


    private static String replaceNonStandardPathSeparator(String featureIdentifier) {
        return featureIdentifier.replace(File.separatorChar, '/');
    }

    private static boolean nonStandardPathSeparatorInUse(String featureIdentifier) {
        return File.separatorChar != '/' && featureIdentifier.contains(File.separator);
    }

    private static URI parseAssumeFileScheme(String featureIdentifier) {
        File featureFile = new File(featureIdentifier);
        if (featureFile.isAbsolute()) {
            return featureFile.toURI();
        }

        try {
            URI root = new File("").toURI();
            URI relative = root.relativize(featureFile.toURI());
            // Scheme is lost by relativize
            return new URI("file", relative.getSchemeSpecificPart(), relative.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}