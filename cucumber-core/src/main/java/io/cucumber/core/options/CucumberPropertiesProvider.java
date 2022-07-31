package io.cucumber.core.options;

@FunctionalInterface
public interface CucumberPropertiesProvider {

    String get(String key);
}
