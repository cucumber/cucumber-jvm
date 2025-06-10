package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

import static org.junit.platform.engine.support.discovery.DiscoveryIssueReporter.deduplicating;
import static org.junit.platform.engine.support.discovery.DiscoveryIssueReporter.forwarding;

class DiscoverySelectorResolver {

    // @formatter:off
    private static final EngineDiscoveryRequestResolver<CucumberEngineDescriptor> resolver = EngineDiscoveryRequestResolver
            .<CucumberEngineDescriptor>builder()
            .addResourceContainerSelectorResolver(new IsFeature())
            .addSelectorResolver(context -> new FeatureResolver(
                    context.getEngineDescriptor().getConfiguration(), //
                    context.getPackageFilter(), //
                    context.getIssueReporter() //
                ))
            .addTestDescriptorVisitor(context -> new OrderingVisitor(
                    context.getDiscoveryRequest().getConfigurationParameters(), //
                    context.getIssueReporter() //
                )
            )
            .addTestDescriptorVisitor(context -> TestDescriptor::prune)
            .build();
    // @formatter:on

    void resolveSelectors(EngineDiscoveryRequest request, CucumberEngineDescriptor engineDescriptor) {
        DiscoveryIssueReporter issueReporter = deduplicating(forwarding( //
            request.getDiscoveryListener(), //
            engineDescriptor.getUniqueId() //
        ));
        resolver.resolve(request, engineDescriptor, issueReporter);
    }

}
