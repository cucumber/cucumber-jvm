package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.ConfigurationParameters;

import java.util.Arrays;
import java.util.stream.Stream;

import static io.cucumber.junit.platform.engine.Constants.READ_SUFFIX;
import static io.cucumber.junit.platform.engine.Constants.READ_WRITE_SUFFIX;
import static java.util.Objects.requireNonNull;

final class ExclusiveResourceConfiguration {

    private final ConfigurationParameters parameters;

    ExclusiveResourceConfiguration(ConfigurationParameters parameters) {
        this.parameters = requireNonNull(parameters);
    }

    public Stream<String> exclusiveReadWriteResource() {
        return parameters.get(READ_WRITE_SUFFIX, s -> Arrays.stream(s.split(","))
                .map(String::trim))
                .orElse(Stream.empty());
    }

    public Stream<String> exclusiveReadResource() {
        return parameters.get(READ_SUFFIX, s -> Arrays.stream(s.split(","))
                .map(String::trim))
                .orElse(Stream.empty());
    }

}
