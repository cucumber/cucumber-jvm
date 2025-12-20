package io.cucumber.core.eventbus;

import org.jspecify.annotations.Nullable;

public interface Options {

    @Nullable
    Class<? extends UuidGenerator> getUuidGeneratorClass();

}
