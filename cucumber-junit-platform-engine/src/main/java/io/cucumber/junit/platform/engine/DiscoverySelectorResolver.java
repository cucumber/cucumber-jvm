package io.cucumber.junit.platform.engine;

import io.cucumber.core.feature.FeatureIdentifier;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

import static io.cucumber.core.feature.FeatureIdentifier.isFeature;

class DiscoverySelectorResolver {

    private static final EngineDiscoveryRequestResolver<CucumberEngineDescriptor> resolver = EngineDiscoveryRequestResolver
            .<CucumberEngineDescriptor> builder()
            .addSelectorResolver(context -> new FileContainerSelectorResolver( //
                FeatureIdentifier::isFeature //
            ))
            .addResourceContainerSelectorResolver(resource -> isFeature(resource.getName()))
            .addSelectorResolver(context -> new FeatureResolver(
                context.getEngineDescriptor().getConfiguration(), //
                context.getPackageFilter(), //
                context.getIssueReporter() //
            ))
            .addTestDescriptorVisitor(context -> new OrderingVisitor(
                context.getDiscoveryRequest().getConfigurationParameters() //
            ))
            .build();

    void resolveSelectors(
            EngineDiscoveryRequest request, CucumberEngineDescriptor engineDescriptor,
            DiscoveryIssueReporter issueReporter
    ) {
        resolver.resolve(request, engineDescriptor, issueReporter);
    }

}
