package io.cucumber.core.feature;

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Identifies either a directory containing feature files, a specific feature or
 * specific scenarios and examples (pickles) in a feature.
 * <p>
 * The syntax of a a feature with lines defined as either a {@link FeaturePath}
 * or a {@link FeatureIdentifier} followed by a sequence of line numbers each
 * preceded by a colon.
 */
public class FeatureWithLines implements Serializable {

    private static final long serialVersionUID = 20190126L;
    private static final Pattern FEATURE_COLON_LINE_PATTERN = Pattern.compile("^(.*?):([\\d:]+)$");
    private static final String INVALID_PATH_MESSAGE = " is not valid. Try <uri or path>/<name>.feature[:LINE]*";

    private final URI uri;
    private final SortedSet<Integer> lines;

    private FeatureWithLines(URI uri, Collection<Integer> lines) {
        this.uri = uri;
        this.lines = Collections.unmodifiableSortedSet(new TreeSet<>(lines));
    }

    public static FeatureWithLines parse(String featurePath) {
        Matcher matcher = FEATURE_COLON_LINE_PATTERN.matcher(featurePath);

        try {
            if (!matcher.matches()) {
                return parseFeaturePath(featurePath);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(featurePath + INVALID_PATH_MESSAGE, e);
        }

        String uriGroup = matcher.group(1);
        if (uriGroup.isEmpty()) {
            throw new IllegalArgumentException(featurePath + INVALID_PATH_MESSAGE);
        }

        try {
            return parseFeatureIdentifierAndLines(uriGroup, matcher.group(2));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(featurePath + INVALID_PATH_MESSAGE, e);
        }
    }

    private static FeatureWithLines parseFeaturePath(String pathName) {
        return create(FeaturePath.parse(pathName), Collections.emptyList());
    }

    private static FeatureWithLines parseFeatureIdentifierAndLines(String uriGroup, String linesGroup) {
        List<Integer> lines = toInts(linesGroup.split(":"));
        return parse(uriGroup, lines);
    }

    public static FeatureWithLines create(URI uri, Collection<Integer> lines) {
        if (lines.isEmpty()) {
            return new FeatureWithLines(uri, lines);
        }

        return new FeatureWithLines(FeatureIdentifier.parse(uri), lines);
    }

    private static List<Integer> toInts(String[] strings) {
        return Arrays.stream(strings)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public static FeatureWithLines parse(String uri, Collection<Integer> lines) {
        return create(FeaturePath.parse(uri), lines);
    }

    public SortedSet<Integer> lines() {
        return lines;
    }

    public URI uri() {
        return uri;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, lines);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FeatureWithLines that = (FeatureWithLines) o;
        return uri.equals(that.uri) && lines.equals(that.lines);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(uri.toString());
        for (Integer line : lines) {
            builder.append(':');
            builder.append(line);
        }
        return builder.toString();
    }

}
