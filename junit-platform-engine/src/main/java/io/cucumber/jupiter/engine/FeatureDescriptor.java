package io.cucumber.jupiter.engine;

import gherkin.ast.Examples;
import gherkin.ast.Feature;
import gherkin.ast.Scenario;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.TableRow;
import io.cucumber.core.feature.CucumberFeature;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.concurrent.atomic.AtomicInteger;

class FeatureDescriptor extends AbstractTestDescriptor implements Node<CucumberEngineExecutionContext> {

    private final CucumberFeature feature;

    private FeatureDescriptor(UniqueId uniqueId, String name, TestSource source, CucumberFeature feature) {
        super(uniqueId, name, source);
        this.feature = feature;
    }

    static TestDescriptor create(CucumberFeature cucumberFeature, TestDescriptor parent) {
        FeatureOrigin source = FeatureOrigin.fromUri(cucumberFeature.getUri());
        Feature feature = cucumberFeature.getGherkinFeature();
        TestDescriptor descriptor = new FeatureDescriptor(
            source.featureSegment(parent.getUniqueId(), cucumberFeature),
            feature.getName(),
            source.featureSource(),
            cucumberFeature
        );
        parent.addChild(descriptor);
        feature.getChildren().forEach(scenarioDefinition -> visit(cucumberFeature, descriptor, source, scenarioDefinition, 0));
        return descriptor;
    }

    private static void visit(CucumberFeature feature, TestDescriptor parent, FeatureOrigin source, gherkin.ast.Node node, int row) {
        if (node instanceof Scenario) {
            Scenario scenario = (Scenario) node;
            feature.getPickleAt(scenario.getLocation().getLine())
                .ifPresent(pickle -> {
                    PickleDescriptor descriptor = new PickleDescriptor(
                        source.scenarioSegment(parent.getUniqueId(), scenario),
                        scenario.getName(),
                        source.nodeSource(scenario),
                        pickle
                    );
                    parent.addChild(descriptor);
                });
        }

        if (node instanceof TableRow) {
            TableRow tableRow = (TableRow) node;
            feature.getPickleAt(tableRow.getLocation().getLine())
                .ifPresent(pickle -> {
                    PickleDescriptor descriptor = new PickleDescriptor(
                        source.exampleSegment(parent.getUniqueId(), tableRow),
                        "Example #" + row,
                        source.nodeSource(tableRow),
                        pickle
                    );
                    parent.addChild(descriptor);
                });
        }

        if (node instanceof ScenarioOutline) {
            ScenarioOutline scenarioOutline = (ScenarioOutline) node;
            NodeDescriptor descriptor = new NodeDescriptor(
                source.outlineSegment(parent.getUniqueId(), scenarioOutline),
                scenarioOutline.getName(),
                source.nodeSource(scenarioOutline)
            );
            parent.addChild(descriptor);
            scenarioOutline.getExamples().forEach(examples -> visit(feature, descriptor, source, examples, row));
            return;
        }

        if (node instanceof Examples) {
            Examples examples = (Examples) node;
            NodeDescriptor descriptor = new NodeDescriptor(
                source.examplesSegment(parent.getUniqueId(), examples),
                examples.getName(),
                source.nodeSource(examples)
            );
            parent.addChild(descriptor);
            AtomicInteger rowCounter = new AtomicInteger(1);
            examples.getTableBody().forEach(tableRow -> visit(feature, descriptor, source, tableRow, rowCounter.getAndIncrement()));
        }

    }

    @Override
    public CucumberEngineExecutionContext prepare(CucumberEngineExecutionContext context) {
        context.beforeFeature(feature);
        return context;
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

}
