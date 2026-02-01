package io.cucumber.core.backend;

import org.jspecify.annotations.Nullable;

public interface Options {

    @Nullable
    Class<? extends ObjectFactory> getObjectFactoryClass();

}
