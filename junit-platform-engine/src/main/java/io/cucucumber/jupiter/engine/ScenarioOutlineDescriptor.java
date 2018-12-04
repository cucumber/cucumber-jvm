package io.cucucumber.jupiter.engine;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

class ScenarioOutlineDescriptor extends AbstractTestDescriptor {

    ScenarioOutlineDescriptor(UniqueId uniqueId, String name, TestSource source) {
        super(uniqueId, name, source);
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

}
