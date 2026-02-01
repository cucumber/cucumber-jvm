package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.FeatureParserException;
import io.cucumber.core.resource.Resource;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;

import java.util.Objects;
import java.util.Optional;

import static org.junit.platform.engine.DiscoveryIssue.Severity.ERROR;

class FeatureParserWithIssueReporting {

    private final FeatureParserWithSource delegate;
    private final DiscoveryIssueReporter issueReporter;

    FeatureParserWithIssueReporting(FeatureParserWithSource delegate, DiscoveryIssueReporter issueReporter) {
        this.delegate = delegate;
        this.issueReporter = issueReporter;
    }

    Optional<FeatureWithSource> parseResource(Resource resource) {
        try {
            return delegate.parseResource(resource);
        } catch (FeatureParserException e) {
            FeatureSource featureSource = FeatureSource.of(resource.getUri());
            issueReporter.reportIssue(DiscoveryIssue
                    // TODO: Improve parse exception to separate out source uri
                    // and individual errors.
                    .builder(ERROR, Objects.requireNonNull(e.getMessage()))
                    .cause(e.getCause())
                    .source(featureSource.source()));
            return Optional.empty();
        }
    }
}
