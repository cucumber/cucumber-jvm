package io.cucumber.jupiter.engine;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

class NodeDescriptor extends AbstractTestDescriptor {

    NodeDescriptor(UniqueId uniqueId, String name, TestSource source) {
        super(uniqueId, name, source);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

}
