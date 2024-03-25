package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.TestDescriptor;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

class FeatureOrderingVisitor implements TestDescriptor.Visitor {

    @Override
    public void visit(TestDescriptor descriptor) {
        // TODO: Make order configurable
        if (descriptor instanceof CucumberEngineDescriptor) {
            List<? extends TestDescriptor> sortedChildren = descriptor.getChildren().stream()
                    .sorted(comparing(testDescriptor -> testDescriptor.getUniqueId().getLastSegment().getValue()))
                    .collect(Collectors.toList());
            sortedChildren.forEach(descriptor::removeChild);
            sortedChildren.forEach(descriptor::addChild);
        }
    }
}
