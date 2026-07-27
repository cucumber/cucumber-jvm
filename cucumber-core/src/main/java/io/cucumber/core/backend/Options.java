package io.cucumber.core.backend;

import org.jspecify.annotations.Nullable;

import java.time.Duration;

public interface Options {

    @Nullable
    Class<? extends ObjectFactory> getObjectFactoryClass();

    boolean isGlueHintEnabled();

    Duration getGlueHintThreshold();

}
