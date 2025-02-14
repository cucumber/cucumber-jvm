package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.TestDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

class OrderingVisitor implements TestDescriptor.Visitor {

    private final UnaryOperator<List<TestDescriptor>> featureOrderer;
    private final UnaryOperator<List<TestDescriptor>> featureElementOrderer;

    public OrderingVisitor(CucumberConfiguration configuration) {
        featureOrderer = configuration.getFeatureOrderer();
        featureElementOrderer = configuration.getFeatureElementOrderer();
    }

    @Override
    public void visit(TestDescriptor descriptor) {
        if (descriptor instanceof FeatureElementDescriptor){
            List<? extends TestDescriptor> sortedChildren = featureOrderer.apply(new ArrayList<>(descriptor.getChildren()));
            sortedChildren.forEach(descriptor::removeChild);
            sortedChildren.forEach(descriptor::addChild);
        } else if (descriptor instanceof CucumberEngineDescriptor) {
            List<? extends TestDescriptor> sortedChildren = featureOrderer.apply(new ArrayList<>(descriptor.getChildren()));
            sortedChildren.forEach(descriptor::removeChild);
            sortedChildren.forEach(descriptor::addChild);
        }
    }

    private static List<TestDescriptor> orderFeatureElements(Set<TestDescriptor> children) {
        return children.stream()
                .sorted(comparing(testDescriptor -> testDescriptor.getUniqueId().getLastSegment().getValue()))
                .collect(Collectors.toList());
    }

    private static List<TestDescriptor> orderFeatures(Set<TestDescriptor> children) {
        return children.stream()
                .filter(FeatureElementDescriptor.class::isInstance)
                .map(FeatureElementDescriptor.class::cast)
                .sorted(comparing(FeatureElementDescriptor::getLine))
                .collect(Collectors.toList());
    }
}
