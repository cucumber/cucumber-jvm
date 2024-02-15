package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.TestDescriptor;

import java.util.function.Predicate;

class PackageFilteringVisitor implements TestDescriptor.Visitor {

    private final Predicate<String> filter;

    public PackageFilteringVisitor(Predicate<String> filter) {
        this.filter = filter;
    }

    @Override
    public void visit(TestDescriptor descriptor) {
        if (descriptor instanceof FeatureElementDescriptor.PickleDescriptor) {
            FeatureElementDescriptor.PickleDescriptor pickleDescriptor = (FeatureElementDescriptor.PickleDescriptor) descriptor;
            boolean include = pickleDescriptor.getPackage()
                    .map(filter::test)
                    .orElse(true);
            if (!include) {
                descriptor.removeFromHierarchy();
            }
        }
    }

}
