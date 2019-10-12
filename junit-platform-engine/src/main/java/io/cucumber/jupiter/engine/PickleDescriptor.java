package io.cucumber.jupiter.engine;

import gherkin.ast.ScenarioDefinition;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.jupiter.engine.resource.ClasspathSupport;
import io.cucumber.core.feature.CucumberPickle;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClasspathResourceSource;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class PickleDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final CucumberPickle pickleEvent;

    private PickleDescriptor(UniqueId uniqueId, String name, TestSource source, CucumberPickle pickleEvent) {
        super(uniqueId, name, source);
        this.pickleEvent = pickleEvent;
    }

    static PickleDescriptor createExample(CucumberPickle pickleEvent, int index, FeatureOrigin source, TestDescriptor parent) {
        UniqueId uniqueId = source.exampleSegment(parent.getUniqueId(), pickleEvent);
        TestSource testSource = source.exampleSource(pickleEvent);
        return new PickleDescriptor(uniqueId, "Example #" + index, testSource, pickleEvent);
    }

    static TestDescriptor createScenario(CucumberFeature feature, ScenarioDefinition scenarioDefinition, FeatureOrigin source, TestDescriptor parent) {
        UniqueId uniqueId = source.scenarioSegment(parent.getUniqueId(), scenarioDefinition);
        TestSource testSource = source.scenarioSource(scenarioDefinition);
        int scenarioLine = scenarioDefinition.getLocation().getLine();
        CucumberPickle pickle = feature.getPickles().stream()
            .filter(cucumberPickle -> {
                return cucumberPickle.getScenarioLine() == scenarioLine;
            }).findFirst().orElseThrow(() -> new IllegalStateException("No pickle for line " + scenarioLine));
        return new PickleDescriptor(uniqueId, pickle.getName(), testSource, pickle);
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    @Override
    public CucumberEngineExecutionContext execute(CucumberEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) {
        context.runTestCase(pickleEvent);
        return context;
    }

    @Override
    public Set<TestTag> getTags() {
        return pickleEvent.getTags().stream()
            .filter(TestTag::isValid)
            .map(TestTag::create)
            .collect(Collectors.toSet());
    }

    Optional<String> getPackage() {
        return getSource()
            .filter(ClasspathResourceSource.class::isInstance)
            .map(ClasspathResourceSource.class::cast)
            .map(ClasspathResourceSource::getClasspathResourceName)
            .map(ClasspathSupport::packageNameOfResource);
    }

}
