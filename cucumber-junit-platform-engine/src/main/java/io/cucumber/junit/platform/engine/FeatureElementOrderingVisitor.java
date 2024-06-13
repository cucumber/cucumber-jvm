package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.TestDescriptor;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

final class FeatureElementOrderingVisitor implements TestDescriptor.Visitor {

    @Override
    public void visit(TestDescriptor descriptor) {
        List<? extends TestDescriptor> sortedChildren = descriptor.getChildren().stream()
                .filter(FeatureElementDescriptor.class::isInstance)
                .map(FeatureElementDescriptor.class::cast)
                .sorted(comparing(FeatureElementDescriptor::getLine))
                .collect(Collectors.toList());
        sortedChildren.forEach(descriptor::removeChild);
        sortedChildren.forEach(descriptor::addChild);
    }
}
