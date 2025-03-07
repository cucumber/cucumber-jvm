package io.cucumber.junit.platform.engine;

import io.cucumber.plugin.event.Location;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import java.net.URI;

abstract class AbstractCucumberTestDescriptor extends AbstractTestDescriptor {

    protected AbstractCucumberTestDescriptor(UniqueId uniqueId, String displayName, TestSource source) {
        super(uniqueId, displayName, source);
    }

    protected abstract URI getUri();

    protected abstract Location getLocation();
}
