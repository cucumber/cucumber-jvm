package io.cucucumber.jupiter.engine;

import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.io.FileResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import gherkin.events.PickleEvent;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UriSelector;
import org.junit.platform.engine.support.descriptor.FileSource;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.cucucumber.jupiter.engine.Classloaders.getDefaultClassLoader;
import static java.util.Collections.singletonList;

final class FeatureResolver {

    static FeatureResolver createFeatureResolver(TestDescriptor engineDescriptor) {
        return new FeatureResolver(engineDescriptor);
    }

    private final TestDescriptor engineDescriptor;

    private FeatureResolver(TestDescriptor engineDescriptor) {
        this.engineDescriptor = engineDescriptor;
    }

    void resolveFile(DirectorySelector selector) {
        new FeatureLoader(new FileResourceLoader())
            .load(singletonList(selector.getRawPath()))
            .forEach(this::resolveFeature);
    }

    void resolveFile(FileSelector selector) {
        new FeatureLoader(new FileResourceLoader())
            .load(singletonList(selector.getRawPath()))
            .forEach(this::resolveFeature);
    }

    void resolvePackageResource(PackageSelector selector) {
        new FeatureLoader(new ClasspathResourceLoader(getDefaultClassLoader()))
            .load(singletonList(selector.getPackageName().replace('.', '/')))
            .forEach(this::resolveFeature);
    }

    void resolveClassPathResource(ClasspathResourceSelector selector) {
        new FeatureLoader(new ClasspathResourceLoader(getDefaultClassLoader()))
            .load(singletonList(selector.getClasspathResourceName()))
            .forEach(this::resolveFeature);
    }

    void resolveClassPathRoot(ClasspathRootSelector selector) {
        new FeatureLoader(new FileResourceLoader())
            .load(singletonList(selector.getClasspathRoot().getPath()))
            .forEach(this::resolveFeature);
    }

    void resolveUri(UriSelector uriSelector) {
        new FeatureLoader(new FileResourceLoader())
            .load(singletonList(uriSelector.getUri().getPath()))
            .forEach(this::resolveFeature);
    }

    private void resolveFeature(CucumberFeature feature) {
        UniqueId featureId = engineDescriptor.getUniqueId().append("feature", feature.getUri());
        TestSource source = FileSource.from(new File(feature.getUri()));
        FeatureFileDescriptor featureFileDescriptor = new FeatureFileDescriptor(featureId, feature, source);
        engineDescriptor.addChild(featureFileDescriptor);

        compileFeature(feature).forEach((scenarioLine, pickleEvents) -> {
            if (pickleEvents.size() == 1) {
                featureFileDescriptor.addScenario(feature, pickleEvents.get(0));
            } else {
                featureFileDescriptor.addScenarioOutline(feature, pickleEvents);
            }
        });
    }

    private Map<Integer, List<PickleEvent>> compileFeature(CucumberFeature feature) {
        Compiler compiler = new Compiler();
        // A scenario with examples compiles into multiple pickles
        // We group these pickles by their original scenario
        Map<Integer, List<PickleEvent>> picklesPerScenario = new LinkedHashMap<>();
        for (Pickle pickle : compiler.compile(feature.getGherkinFeature())) {
            List<PickleLocation> locations = pickle.getLocations();
            int scenarioLocation = locations.get(locations.size() - 1).getLine();
            picklesPerScenario.putIfAbsent(scenarioLocation, new ArrayList<>());
            picklesPerScenario.get(scenarioLocation).add(new PickleEvent(feature.getUri(), pickle));
        }

        return picklesPerScenario;
    }

}
