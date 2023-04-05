package io.cucumber.junit.platform.engine;

import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.junit.platform.engine.NodeDescriptor.PickleDescriptor;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.discovery.UriSelector;

import java.util.List;
import java.util.function.Predicate;

import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.FeatureResolver.create;
import static org.junit.platform.engine.Filter.composeFilters;

class DiscoverySelectorResolver {

    private static final Logger log = LoggerFactory.getLogger(DiscoverySelectorResolver.class);

    private static boolean warnedWhenCucumberFeaturesPropertyIsUsed = false;

    private static void warnWhenCucumberFeaturesPropertyIsUsed() {
        if (warnedWhenCucumberFeaturesPropertyIsUsed) {
            return;
        }
        warnedWhenCucumberFeaturesPropertyIsUsed = true;
        log.warn(
            () -> "Discovering tests using the " + FEATURES_PROPERTY_NAME
                    + " property. Other discovery selectors are ignored!\n" +
                    "Please request/upvote/sponsor/ect better support for JUnit 5 discovery selectors.\n" +
                    "See: https://github.com/cucumber/cucumber-jvm/pull/2498");
    }

    void resolveSelectors(EngineDiscoveryRequest request, CucumberEngineDescriptor engineDescriptor) {
        Predicate<String> packageFilter = buildPackageFilter(request);
        resolve(request, engineDescriptor, packageFilter);
        filter(engineDescriptor, packageFilter);
        pruneTree(engineDescriptor);
    }

    private Predicate<String> buildPackageFilter(EngineDiscoveryRequest request) {
        Filter<String> packageFilter = composeFilters(request.getFiltersByType(PackageNameFilter.class));
        return packageFilter.toPredicate();
    }

    private void resolve(
            EngineDiscoveryRequest request, CucumberEngineDescriptor engineDescriptor, Predicate<String> packageFilter
    ) {
        ConfigurationParameters configuration = request.getConfigurationParameters();
        FeatureResolver featureResolver = create(
            configuration,
            engineDescriptor,
            packageFilter);

        CucumberEngineOptions options = new CucumberEngineOptions(configuration);
        List<FeatureWithLines> featureWithLines = options.featuresWithLines();
        if (!featureWithLines.isEmpty()) {
            warnWhenCucumberFeaturesPropertyIsUsed();
            featureWithLines.forEach(featureResolver::resolveFeatureWithLines);
            return;
        }

        request.getSelectorsByType(ClasspathRootSelector.class).forEach(featureResolver::resolveClasspathRoot);
        request.getSelectorsByType(ClasspathResourceSelector.class).forEach(featureResolver::resolveClasspathResource);
        request.getSelectorsByType(ClassSelector.class).forEach(featureResolver::resolveClass);
        request.getSelectorsByType(PackageSelector.class).forEach(featureResolver::resolvePackageResource);
        request.getSelectorsByType(FileSelector.class).forEach(featureResolver::resolveFile);
        request.getSelectorsByType(DirectorySelector.class).forEach(featureResolver::resolveDirectory);
        request.getSelectorsByType(UniqueIdSelector.class).forEach(featureResolver::resolveUniqueId);
        request.getSelectorsByType(UriSelector.class).forEach(featureResolver::resolveUri);
    }

    private void filter(TestDescriptor engineDescriptor, Predicate<String> packageFilter) {
        applyPackagePredicate(packageFilter, engineDescriptor);
    }

    private void pruneTree(TestDescriptor rootDescriptor) {
        rootDescriptor.accept(TestDescriptor::prune);
    }

    private void applyPackagePredicate(Predicate<String> packageFilter, TestDescriptor engineDescriptor) {
        engineDescriptor.accept(descriptor -> {
            if (descriptor instanceof PickleDescriptor) {
                PickleDescriptor pickleDescriptor = (PickleDescriptor) descriptor;
                if (!includePickle(pickleDescriptor, packageFilter)) {
                    descriptor.removeFromHierarchy();
                }
            }
        });
    }

    private boolean includePickle(PickleDescriptor pickleDescriptor, Predicate<String> packageFilter) {
        return pickleDescriptor.getPackage()
                .map(packageFilter::test)
                .orElse(true);
    }

}
