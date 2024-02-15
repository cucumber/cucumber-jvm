package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.TestDescriptor;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

final class FeatureElementOrderingVisitor implements TestDescriptor.Visitor {

    @Override
    public void visit(TestDescriptor descriptor) {
        if (descriptor instanceof FeatureDescriptor || descriptor instanceof FeatureElementDescriptor) {
            List<? extends TestDescriptor> sortedChildren = descriptor.getChildren().stream()
                    // TODO: Use file location instead. Problem. UriSource
                    // doesn't have one.
                    .sorted(comparing(
                        testDescriptor -> Integer.valueOf(testDescriptor.getUniqueId().getLastSegment().getValue())))
                    .collect(Collectors.toList());
            sortedChildren.forEach(descriptor::removeChild);
            sortedChildren.forEach(descriptor::addChild);
        }
    }
}
