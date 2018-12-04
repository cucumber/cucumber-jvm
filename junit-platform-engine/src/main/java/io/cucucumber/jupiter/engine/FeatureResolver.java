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
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UriSelector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static io.cucucumber.jupiter.engine.Classloaders.getDefaultClassLoader;
import static io.cucucumber.jupiter.engine.FeatureSource.fromFeature;
import static io.cucucumber.jupiter.engine.FeatureSource.fromPickle;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

final class FeatureResolver {

    private final TestDescriptor engineDescriptor;

    private FeatureResolver(TestDescriptor engineDescriptor) {
        this.engineDescriptor = engineDescriptor;
    }

    static FeatureResolver createFeatureResolver(TestDescriptor engineDescriptor) {
        return new FeatureResolver(engineDescriptor);
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
            .forEach(this::resolveFeatureFromClassPath);
    }

    void resolveClassPathResource(ClasspathResourceSelector selector) {
        new FeatureLoader(new ClasspathResourceLoader(getDefaultClassLoader()))
            .load(singletonList(selector.getClasspathResourceName()))
            .forEach(this::resolveFeatureFromClassPath);
    }

    void resolveClassPathRoot(ClasspathRootSelector selector) {
        new FeatureLoader(new FileResourceLoader())
            .load(singletonList(selector.getClasspathRoot().getPath()))
            .forEach(this::resolveFeatureFromClassPath);
    }

    void resolveUri(UriSelector uriSelector) {
        new FeatureLoader(new FileResourceLoader())
            .load(singletonList(uriSelector.getUri().getPath()))
            .forEach(this::resolveFeature);
    }

    private void resolveFeature(CucumberFeature feature) {
        resolveFeature2(feature, false);
    }

    private void resolveFeatureFromClassPath(CucumberFeature cucumberFeature) {
        resolveFeature2(cucumberFeature, true);
    }

    private void resolveFeature2(CucumberFeature cucumberFeature, boolean inPackage) {
        new TestDescriptorAdder(cucumberFeature, inPackage).add(compileFeature(cucumberFeature), engineDescriptor);
    }

    private Node compileFeature(CucumberFeature feature) {
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

        List<Node> nodes = new ArrayList<>();
        picklesPerScenario.forEach((line, pickleEvents) -> {
            if (pickleEvents.size() == 1) {
                nodes.add(new Scenario(line, pickleEvents.get(0)));
            } else {
                AtomicInteger counter = new AtomicInteger(0);
                nodes.add(new ScenarioOutline(line, pickleEvents.stream()
                    .map(pickleEvent -> new Example(counter.getAndIncrement(), pickleEvent))
                    .collect(Collectors.toList())));
            }
        });

        return new Feature(feature, nodes);
    }

    interface Visitor {

        TestDescriptor add(Feature node, TestDescriptor parent);

        TestDescriptor map(Scenario node, TestDescriptor parent);

        TestDescriptor add(ScenarioOutline node, TestDescriptor parent);

        TestDescriptor add(Example node, TestDescriptor parent);
    }

    interface Node {

        default List<? extends Node> getChildren() {
            return emptyList();
        }

        TestDescriptor doAccept(Visitor visitor, TestDescriptor parent);

    }

    static class Feature implements Node {
        private final CucumberFeature cucumberFeature;
        private final List<? extends Node> scenarios;

        Feature(CucumberFeature cucumberFeature, List<? extends Node> scenarios) {
            this.cucumberFeature = cucumberFeature;
            this.scenarios = scenarios;
        }

        @Override
        public List<? extends Node> getChildren() {
            return scenarios;
        }

        @Override
        public TestDescriptor doAccept(Visitor visitor, TestDescriptor parent) {
            return visitor.add(this, parent);
        }

        String getName() {
            return cucumberFeature.getGherkinFeature().getFeature().getName();
        }
    }

    static class Scenario implements Node {

        private final PickleEvent pickleEvent;
        private int line;

        Scenario(int line, PickleEvent pickleEvent) {
            this.line = line;
            this.pickleEvent = pickleEvent;
        }

        PickleEvent getPickle() {
            return pickleEvent;
        }

        String getLine() {
            return String.valueOf(line);
        }


        @Override
        public TestDescriptor doAccept(Visitor visitor, TestDescriptor parent) {
            return visitor.map(this, parent);
        }
    }

    static class ScenarioOutline implements Node {

        private final List<Example> examples;
        private final int line;

        ScenarioOutline(int line, List<Example> examples) {
            this.examples = examples;
            this.line = line;
        }

        @Override
        public List<? extends Node> getChildren() {
            return examples;
        }

        String getLine() {
            return String.valueOf(line);
        }

        PickleEvent getPickle() {
            return examples.get(0).getPickle();
        }

        String getName() {
            return examples.get(0).getPickle().pickle.getName();
        }

        @Override
        public TestDescriptor doAccept(Visitor visitor, TestDescriptor parent) {
            return visitor.add(this, parent);
        }
    }

    static class Example implements Node {

        private final PickleEvent pickle;
        private final int index;

        Example(int index, PickleEvent pickle) {
            this.index = index;
            this.pickle = pickle;
        }

        PickleEvent getPickle() {
            return pickle;
        }

        String getLine() {
            return String.valueOf(pickle.pickle.getLocations().get(0).getLine());
        }

        String getIndex() {
            return String.valueOf(index);
        }

        @Override
        public TestDescriptor doAccept(Visitor visitor, TestDescriptor parent) {
            return visitor.add(this, parent);
        }
    }

    private static class TestDescriptorAdder implements Visitor {

        private final CucumberFeature cucumberFeature;
        private final boolean inPackage;

        TestDescriptorAdder(CucumberFeature cucumberFeature, boolean inPackage) {
            this.cucumberFeature = cucumberFeature;
            this.inPackage = inPackage;
        }

        void add(Node node, TestDescriptor parent) {
            TestDescriptor testDescriptor = node.doAccept(this, parent);
            Optional<? extends TestDescriptor> byUniqueId = parent.findByUniqueId(testDescriptor.getUniqueId());
            if (byUniqueId.isPresent()) {
                node.getChildren().forEach(child -> this.add(child, byUniqueId.get()));
                return;
            }
            parent.addChild(testDescriptor);
            node.getChildren().forEach(child -> this.add(child, testDescriptor));
        }

        @Override
        public TestDescriptor add(Feature node, TestDescriptor parent) {
            UniqueId uniqueId = parent.getUniqueId().append("feature", cucumberFeature.getUri());
            return new FeatureFileDescriptor(uniqueId, node.getName(), fromFeature(cucumberFeature), cucumberFeature);
        }

        @Override
        public TestDescriptor map(Scenario node, TestDescriptor parent) {
            UniqueId uniqueId = parent.getUniqueId().append("scenario", node.getLine());
            return new PickleDescriptor(uniqueId, fromPickle(cucumberFeature, node.getPickle()), node.getPickle(), inPackage);
        }

        @Override
        public TestDescriptor add(ScenarioOutline node, TestDescriptor parent) {
            UniqueId uniqueId = parent.getUniqueId().append("outline", String.valueOf(node.getLine()));
            return new ScenarioOutlineDescriptor(uniqueId, node.getName(), fromPickle(cucumberFeature, node.getPickle()));
        }

        @Override
        public TestDescriptor add(Example node, TestDescriptor parent) {
            UniqueId uniqueId = parent.getUniqueId().append("example", node.getLine());
            return new PickleDescriptor(uniqueId, "Example #" + node.getIndex(), fromPickle(cucumberFeature, node.getPickle()), node.getPickle(), inPackage);
        }
    }
}
