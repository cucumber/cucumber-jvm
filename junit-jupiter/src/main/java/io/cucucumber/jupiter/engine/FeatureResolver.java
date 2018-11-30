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

    void resolveFileResource(String featurePath) {
        new FeatureLoader(new FileResourceLoader())
            .load(singletonList(featurePath))
            .forEach(this::resolveFeature);
    }

    void resolvePackageResource(String packageName) {
        resolveClassPathResource(packageName.replace('.', '/'));
    }

    void resolveClassPathResource(String classpathResourceName) {
        new FeatureLoader(new ClasspathResourceLoader(getDefaultClassLoader()))
            .load(singletonList(classpathResourceName))
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
