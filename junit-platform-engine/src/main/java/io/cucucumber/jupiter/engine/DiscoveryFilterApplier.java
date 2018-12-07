package io.cucucumber.jupiter.engine;

import org.junit.platform.engine.TestDescriptor;

import java.util.function.Predicate;

class DiscoveryFilterApplier {

    void applyPackagePredicate(Predicate<String> packagePredicate, TestDescriptor engineDescriptor) {
        engineDescriptor.accept(descriptor -> {
            if (descriptor instanceof PickleDescriptor
                && !includePickle((PickleDescriptor) descriptor, packagePredicate)) {
                descriptor.removeFromHierarchy();
            }
        });
    }

    private boolean includePickle(PickleDescriptor pickleDescriptor, Predicate<String> packagePredicate) {
        return pickleDescriptor.getPackage()
            .map(packagePredicate::test)
            .orElse(true);
    }
}
