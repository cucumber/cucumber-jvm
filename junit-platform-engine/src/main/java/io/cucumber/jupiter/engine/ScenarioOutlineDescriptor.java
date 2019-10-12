package io.cucumber.jupiter.engine;

import gherkin.ast.ScenarioOutline;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberPickle;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import java.util.Iterator;

import static io.cucumber.jupiter.engine.PickleDescriptor.createExample;

class ScenarioOutlineDescriptor extends AbstractTestDescriptor {

    private ScenarioOutlineDescriptor(UniqueId uniqueId, String name, TestSource source) {
        super(uniqueId, name, source);
    }

    static TestDescriptor createOutlineDescriptor(CucumberFeature feature, ScenarioOutline scenarioOutline, FeatureOrigin source, TestDescriptor parent) {
        UniqueId uniqueId = source.outlineSegment(parent.getUniqueId(), scenarioOutline);
        TestSource testSource = source.outlineSource(scenarioOutline);
        TestDescriptor descriptor = new ScenarioOutlineDescriptor(uniqueId, scenarioOutline.getName(), testSource);

        Iterator<CucumberPickle> iterator = feature.getPickles().stream()
            .filter(cucumberPickle -> {
                int pickleScenarioLine = cucumberPickle.getScenarioLine();
                int scenarioLine = scenarioOutline.getLocation().getLine();
                return pickleScenarioLine == scenarioLine;
            }).iterator();

        int index = 1;
        while(iterator.hasNext()){
            CucumberPickle cucumberPickle = iterator.next();
            descriptor.addChild(createExample(cucumberPickle, index++, source, descriptor));
        }
        return descriptor;
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

}
