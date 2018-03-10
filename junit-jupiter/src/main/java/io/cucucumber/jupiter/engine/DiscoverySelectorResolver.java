

package io.cucucumber.jupiter.engine;

import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.io.FileResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.discovery.UriSelector;
import org.junit.platform.engine.support.descriptor.FilePosition;
import org.junit.platform.engine.support.descriptor.FileSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.cucucumber.jupiter.engine.Classloaders.getDefaultClassLoader;
import static java.util.Collections.singletonList;

public class DiscoverySelectorResolver {

    public void resolveSelectors(EngineDiscoveryRequest request, TestDescriptor engineDescriptor) {
        resolve(request, engineDescriptor);
        filter(engineDescriptor);
        pruneTree(engineDescriptor);
    }

    private void resolve(EngineDiscoveryRequest request, TestDescriptor engineDescriptor) {
        FeatureResolver featureResolver = createFeatureResolver(engineDescriptor);

        request.getSelectorsByType(ModuleSelector.class).forEach(selector -> {
            //TODO: Find all features in a module
        });
        request.getSelectorsByType(ClasspathRootSelector.class).forEach(selector -> {
            //TODO:
        });
        request.getSelectorsByType(ClasspathResourceSelector.class).forEach(selector -> {
            featureResolver.resolveClassPathResource(selector.getClasspathResourceName());
        });
        request.getSelectorsByType(PackageSelector.class).forEach(selector -> {
            featureResolver.resolveClassPathResource(selector.getPackageName());
        });

        request.getSelectorsByType(FileSelector.class).forEach(selector -> {
            featureResolver.resolveFileResource(selector.getRawPath());
        });

        request.getSelectorsByType(DirectorySelector.class).forEach(selector -> {
            featureResolver.resolveFileResource(selector.getRawPath());
        });
        request.getSelectorsByType(UniqueIdSelector.class).forEach(selector -> {
            //TODO: Find by unique id
        });
        request.getSelectorsByType(UriSelector.class).forEach(selector -> {
            //TODO:
        });

        //TODO: More?
    }

    private void filter(TestDescriptor engineDescriptor) {

    }

    private void pruneTree(TestDescriptor rootDescriptor) {
        rootDescriptor.accept(TestDescriptor::prune);
    }

    private FeatureResolver createFeatureResolver(TestDescriptor engineDescriptor) {
        return new FeatureResolver(engineDescriptor);
    }

    static final class FeatureResolver {


        private final TestDescriptor engineDescriptor;

        FeatureResolver(TestDescriptor engineDescriptor) {
            this.engineDescriptor = engineDescriptor;
        }

        void resolveFileResource(String featurePath) {
            CucumberFeature
                .load(new FileResourceLoader(), singletonList(featurePath))
                .forEach(this::resolveFeature);
        }

        void resolveClassPathResource(String packageName) {
            CucumberFeature
                .load(new ClasspathResourceLoader(getDefaultClassLoader()), singletonList(packageName))
                .forEach(this::resolveFeature);
        }

        private void resolveFeature(CucumberFeature feature) {
            UniqueId featureId = engineDescriptor.getUniqueId().append("feature", feature.getUri());
            TestSource source = FileSource.from(new File(feature.getUri()));
            TestDescriptor featureFileDescriptor = new FeatureFileDescriptor(featureId, feature, source);
            engineDescriptor.addChild(featureFileDescriptor);

            compileFeature(feature).forEach(pickleEvent -> {
                PickleLocation location = pickleEvent.pickle.getLocations().get(0);
                UniqueId scenarioId = featureFileDescriptor.getUniqueId().append("scenario", String.valueOf(location.getLine()));
                TestSource scenarioSource = FileSource.from(new File(feature.getUri()), FilePosition.from(location.getLine(), location.getColumn()));
                TestDescriptor scenarioDescriptor = new ScenarioDescriptor(scenarioId, pickleEvent, scenarioSource);
                featureFileDescriptor.addChild(scenarioDescriptor);

            });
        }

        List<PickleEvent> compileFeature(CucumberFeature feature) {
            Compiler compiler = new Compiler();
            List<PickleEvent> pickleEvents = new ArrayList<PickleEvent>();
            for (Pickle pickle : compiler.compile(feature.getGherkinFeature())) {
                pickleEvents.add(new PickleEvent(feature.getUri(), pickle));
            }
            return pickleEvents;
        }


    }

}
