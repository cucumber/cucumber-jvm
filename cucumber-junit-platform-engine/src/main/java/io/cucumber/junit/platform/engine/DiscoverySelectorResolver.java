package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

class DiscoverySelectorResolver {

    // @formatter:off
    private static final EngineDiscoveryRequestResolver<CucumberEngineDescriptor> resolver = EngineDiscoveryRequestResolver
            .<CucumberEngineDescriptor>builder()
            .addResourceContainerSelectorResolver(new IsFeature())
            .addSelectorResolver(context -> new FeatureResolver(context.getDiscoveryRequest().getConfigurationParameters()))
            .addTestDescriptorVisitor(context -> new FeatureOrderingVisitor())
            .addTestDescriptorVisitor(context -> new FeatureElementOrderingVisitor())
            .addTestDescriptorVisitor(context -> TestDescriptor::prune)
            .build();
    // @formatter:on

    void resolveSelectors(EngineDiscoveryRequest request, CucumberEngineDescriptor engineDescriptor) {
        resolver.resolve(request, engineDescriptor);
    }

}
