package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

import static io.cucumber.core.feature.FeatureIdentifier.isFeature;
import static io.cucumber.junit.platform.engine.FeatureWithLinesFileResolver.isTxtFile;

class DiscoverySelectorResolver {

    private static final EngineDiscoveryRequestResolver<CucumberEngineDescriptor> resolver = EngineDiscoveryRequestResolver
            .<CucumberEngineDescriptor> builder()
            .addSelectorResolver(context -> new FileContainerSelectorResolver( //
                path -> isFeature(path) || isTxtFile(path)))
            .addResourceContainerSelectorResolver(resource -> isFeature(resource.getName()))
            .addSelectorResolver(context -> new FeatureWithLinesFileResolver())
            .addSelectorResolver(context -> new FeatureFileResolver(
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
