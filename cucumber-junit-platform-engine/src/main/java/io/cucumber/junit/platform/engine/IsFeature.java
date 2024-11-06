package io.cucumber.junit.platform.engine;

import org.junit.platform.commons.support.Resource;

import java.util.function.Predicate;

class IsFeature implements Predicate<Resource> {
    @Override
    public boolean test(Resource resource) {
        return resource.getName().endsWith(".feature");
    }
}
