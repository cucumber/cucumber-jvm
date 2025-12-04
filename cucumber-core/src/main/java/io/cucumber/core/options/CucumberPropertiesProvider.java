package io.cucumber.core.options;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface CucumberPropertiesProvider {

    @Nullable String get(String key);
}
