package io.cucumber.core.feature;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME_PREFIX;
import static io.cucumber.core.resource.ClasspathSupport.RESOURCE_SEPARATOR_CHAR;
import static io.cucumber.core.resource.ClasspathSupport.rootPackageUri;
import static java.util.Objects.requireNonNull;

/**
 * A feature path is a URI to a single feature file or directory of features.
 * <p>
 * This URI can either be absolute: {@code scheme:/absolute/path/to.feature}, or
 * relative to the current working directory:
 * {@code scheme:relative/path/to.feature}. In either form, when the scheme is
 * omitted {@code file} will be assumed.
 * <p>
 * On systems that use a {@code File.separatorChar} other then `{@code /}`
 * {@code File.separatorChar} can be used as a path separator. When doing so
 * when the scheme must be omitted: {@code path\to.feature}. <em>It is
 * recommended to use `{@code /}` as the path separator.</em>
 *
 * @see FeatureIdentifier
 * @see FeatureWithLines
 */
public class FeaturePath {

    private static final Logger log = LoggerFactory.getLogger(FeaturePath.class);

    private static final Pattern FILESYSTEM_PATH_TO_RESOURCES = Pattern.compile("src/(?:main|test)/resources/?(.*)");

    private FeaturePath() {

    }

    public static URI parse(String featureIdentifier) {
        requireNonNull(featureIdentifier, "featureIdentifier may not be null");
        if (featureIdentifier.isEmpty()) {
            throw new IllegalArgumentException("featureIdentifier may not be empty");
        }

        // Legacy from the Cucumber Eclipse plugin
        // Older versions of Cucumber allowed it.
        if (CLASSPATH_SCHEME_PREFIX.equals(featureIdentifier)) {
            return rootPackageUri();
        }

        if (nonStandardPathSeparatorInUse(featureIdentifier)) {
            String standardized = replaceNonStandardPathSeparator(featureIdentifier);
            return parseAssumeFileScheme(standardized);
        }

        if (isWindowsOS() && pathContainsWindowsDrivePattern(featureIdentifier)) {
            return parseAssumeFileScheme(featureIdentifier);
        }

        if (probablyURI(featureIdentifier)) {
            return parseProbableURI(featureIdentifier);
        }

        return parseAssumeFileScheme(featureIdentifier);
    }

    private static boolean nonStandardPathSeparatorInUse(String featureIdentifier) {
        return File.separatorChar != RESOURCE_SEPARATOR_CHAR
                && featureIdentifier.contains(File.separator);
    }

    private static String replaceNonStandardPathSeparator(String featureIdentifier) {
        return featureIdentifier.replace(File.separatorChar, RESOURCE_SEPARATOR_CHAR);
    }

    private static URI parseAssumeFileScheme(String featureIdentifier) {
        warnWhenFileSystemPathToResources(featureIdentifier);
        File featureFile = new File(featureIdentifier);
        return featureFile.toURI();
    }

    private static boolean isWindowsOS() {
        String osName = System.getProperty("os.name");
        return normalize(osName).contains("windows");
    }

    private static boolean pathContainsWindowsDrivePattern(String featureIdentifier) {
        return featureIdentifier.matches("^[a-zA-Z]:.*$");
    }

    private static boolean probablyURI(String featureIdentifier) {
        return featureIdentifier.matches("^[a-zA-Z+.\\-]+:.*$");
    }

    private static URI parseProbableURI(String featureIdentifier) {
        URI uri = URI.create(featureIdentifier);
        if ("file".equals(uri.getScheme())) {
            return parseAssumeFileScheme(uri.getSchemeSpecificPart());
        }
        return uri;
    }

    private static String normalize(final String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
    }

    private static void warnWhenFileSystemPathToResources(String featureIdentifier) {
        Matcher matcher = FILESYSTEM_PATH_TO_RESOURCES.matcher(featureIdentifier);
        if (matcher.matches()) {
            log.warn(() -> String.format("Please replace feature path '%s' with the classpath '%s' to avoid ambiguity.",
                featureIdentifier,
                matcher.replaceAll("classpath:$1")));
        }
    }

}
