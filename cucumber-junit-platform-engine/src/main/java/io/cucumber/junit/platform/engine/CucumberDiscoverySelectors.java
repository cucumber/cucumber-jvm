package io.cucumber.junit.platform.engine;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.plugin.event.Node;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.FilePosition;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;

import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.junit.platform.engine.DiscoveryIssue.Severity.WARNING;

class CucumberDiscoverySelectors {

    static final class FeatureWithLinesSelector implements DiscoverySelector {
        private final URI uri;
        private final Set<FilePosition> filePositions;

        private FeatureWithLinesSelector(URI uri, Set<FilePosition> filePositions) {
            this.uri = requireNonNull(uri);
            this.filePositions = requireNonNull(filePositions);
        }

        static FeatureWithLinesSelector from(FeatureWithLines featureWithLines) {
            Set<FilePosition> lines = featureWithLines.lines().stream()
                    .map(FilePosition::from)
                    .collect(Collectors.toSet());
            return new FeatureWithLinesSelector(featureWithLines.uri(), lines);
        }

        static Set<FeatureWithLinesSelector> from(UniqueId uniqueId) {
            return uniqueId.getSegments()
                    .stream()
                    .filter(CucumberTestDescriptor::isFeatureSegment)
                    .map(featureSegment -> {
                        URI uri = URI.create(featureSegment.getValue());
                        Set<FilePosition> filePosition = getFilePosition(uniqueId.getLastSegment());
                        return new FeatureWithLinesSelector(uri, filePosition);
                    })
                    .collect(Collectors.toSet());
        }

        static FeatureWithLinesSelector from(URI uri) {
            Set<FilePosition> positions = FilePosition.fromQuery(uri.getQuery())
                    .map(Collections::singleton)
                    .orElseGet(Collections::emptySet);
            return new FeatureWithLinesSelector(stripQuery(uri), positions);
        }

        private static URI stripQuery(URI uri) {
            if (uri.getQuery() == null) {
                return uri;
            }
            String uriString = uri.toString();
            return URI.create(uriString.substring(0, uriString.indexOf('?')));
        }

        private static Set<FilePosition> getFilePosition(UniqueId.Segment segment) {
            if (CucumberTestDescriptor.isFeatureSegment(segment)) {
                return Collections.emptySet();
            }

            int line = Integer.parseInt(segment.getValue());
            return Collections.singleton(FilePosition.from(line));
        }

        URI getUri() {
            return uri;
        }

        Optional<Set<FilePosition>> getFilePositions() {
            return filePositions.isEmpty() ? Optional.empty() : Optional.of(filePositions);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            FeatureWithLinesSelector that = (FeatureWithLinesSelector) o;
            return uri.equals(that.uri) && filePositions.equals(that.filePositions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri, filePositions);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this) //
                    .append("uri", this.uri) //
                    .append("filePositions", this.filePositions) //
                    .toString();
        }
    }

    static class FeatureElementSelector implements DiscoverySelector {

        private final FeatureWithSource feature;
        private final Node element;

        private FeatureElementSelector(FeatureWithSource feature) {
            this(feature, feature.getFeature());
        }

        private FeatureElementSelector(FeatureWithSource feature, Node element) {
            this.feature = requireNonNull(feature);
            this.element = requireNonNull(element);
        }

        static FeatureElementSelector selectFeature(FeatureWithSource feature) {
            return new FeatureElementSelector(feature);
        }

        static FeatureElementSelector selectElement(FeatureWithSource feature, Node element) {
            return new FeatureElementSelector(feature, element);
        }

        static Stream<FeatureElementSelector> selectElementsAt(
                FeatureWithSource feature, Supplier<Optional<Set<FilePosition>>> filePositions,
                DiscoveryIssueReporter issueReporter
        ) {
            return filePositions.get()
                    .map(positions -> selectElementsAt(feature, positions, issueReporter))
                    .orElseGet(() -> Stream.of(selectFeature(feature)));
        }

        private static Stream<FeatureElementSelector> selectElementsAt(
                FeatureWithSource feature, Set<FilePosition> filePositions, DiscoveryIssueReporter issueReporter
        ) {
            return filePositions.stream().map(filePosition -> selectElementAt(feature, filePosition, issueReporter));
        }

        static FeatureElementSelector selectElementAt(
                FeatureWithSource feature, Supplier<Optional<FilePosition>> filePosition,
                DiscoveryIssueReporter issueReporter
        ) {
            return filePosition.get()
                    .map(position -> selectElementAt(feature, position, issueReporter))
                    .orElseGet(() -> selectFeature(feature));
        }

        static FeatureElementSelector selectElementAt(
                FeatureWithSource feature, FilePosition filePosition, DiscoveryIssueReporter issueReporter
        ) {
            return feature.getFeature()
                    .findPathTo(candidate -> candidate.getLocation().getLine() == filePosition.getLine())
                    .map(nodes -> nodes.get(nodes.size() - 1))
                    .map(node -> new FeatureElementSelector(feature, node))
                    .orElseGet(() -> {
                        reportInvalidFilePosition(feature, filePosition, issueReporter);
                        return selectFeature(feature);
                    });
        }

        private static void reportInvalidFilePosition(
                FeatureWithSource feature, FilePosition filePosition, DiscoveryIssueReporter issueReporter
        ) {
            issueReporter.reportIssue(DiscoveryIssue.create(WARNING,
                "Feature file " + feature.getUri()
                        + " does not have a feature, rule, scenario, or example element at line "
                        + filePosition.getLine() +
                        ". Selecting the whole feature instead"));
        }

        static Set<FeatureElementSelector> selectElementsOf(FeatureWithSource feature, Node selected) {
            if (selected instanceof Node.Container) {
                Node.Container<?> container = (Node.Container<?>) selected;
                return container.elements().stream()
                        .map(element -> new FeatureElementSelector(feature, element))
                        .collect(toSet());
            }
            return Collections.emptySet();
        }

        FeatureWithSource getFeature() {
            return feature;
        }

        Node getElement() {
            return element;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            FeatureElementSelector that = (FeatureElementSelector) o;
            return feature.equals(that.feature) && element.equals(that.element);
        }

        @Override
        public int hashCode() {
            return Objects.hash(feature, element);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this) //
                    .append("feature", this.feature.getUri()) //
                    .append("element", this.element.getLocation()) //
                    .toString();
        }
    }
}
