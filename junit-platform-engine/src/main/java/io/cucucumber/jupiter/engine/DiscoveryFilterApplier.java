package io.cucucumber.jupiter.engine;

import io.cucucumber.jupiter.engine.resource.ClassFilter;
import org.junit.platform.engine.TestDescriptor;

class DiscoveryFilterApplier {

    void applyPackagePredicate(ClassFilter packagePredicate, TestDescriptor engineDescriptor) {
        engineDescriptor.accept(descriptor -> {
            if (descriptor instanceof PickleDescriptor
                && !includePickle((PickleDescriptor) descriptor, packagePredicate)) {
                descriptor.removeFromHierarchy();
            }
        });
    }

    private boolean includePickle(PickleDescriptor pickleDescriptor, ClassFilter packagePredicate) {
        return pickleDescriptor.getPackage()
            .map(packagePredicate::match)
            .orElse(true);
    }

}
