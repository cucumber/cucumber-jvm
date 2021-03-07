package io.cucumber.cdi2.spi;

import javax.enterprise.inject.se.SeContainerInitializer;

public interface SeContainerInitializerCustomizer {
    void customize(SeContainerInitializer initializer);
}
