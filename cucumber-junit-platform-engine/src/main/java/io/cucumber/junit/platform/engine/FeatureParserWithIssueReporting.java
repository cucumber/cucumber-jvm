package io.cucumber.junit.platform.engine;

import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.FeatureParserException;
import io.cucumber.core.resource.Resource;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;

import java.util.Optional;

import static org.junit.platform.engine.DiscoveryIssue.Severity.ERROR;

class FeatureParserWithIssueReporting {

    private final FeatureParser delegate;
    private final DiscoveryIssueReporter issueReporter;

    FeatureParserWithIssueReporting(FeatureParser delegate, DiscoveryIssueReporter issueReporter) {
        this.delegate = delegate;
        this.issueReporter = issueReporter;
    }

    Optional<Feature> parseResource(Resource resource) {
        try {
            return delegate.parseResource(resource);
        } catch (FeatureParserException e) {
            FeatureOrigin featureOrigin = FeatureOrigin.fromUri(resource.getUri());
            issueReporter.reportIssue(DiscoveryIssue
                    // TODO: Improve parse exception to separate out source uri
                    // and individual errors.
                    .builder(ERROR, e.getMessage())
                    .cause(e.getCause())
                    .source(featureOrigin.source()));
            return Optional.empty();
        }
    }
}
