package io.cucumber.junit.platform.engine;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;

import java.util.Arrays;
import java.util.stream.Stream;

import static io.cucumber.junit.platform.engine.Constants.READ_SUFFIX;
import static io.cucumber.junit.platform.engine.Constants.READ_WRITE_SUFFIX;
import static java.util.Objects.requireNonNull;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode.READ;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode.READ_WRITE;

final class ExclusiveResourceConfiguration {

    private final ConfigurationParameters configuration;

    ExclusiveResourceConfiguration(ConfigurationParameters configuration) {
        this.configuration = requireNonNull(configuration);
    }

    private Stream<String> exclusiveReadWriteResource() {
        return configuration.get(READ_WRITE_SUFFIX, s -> Arrays.stream(s.split(","))
                .map(String::trim))
                .orElse(Stream.empty());
    }

    private Stream<String> exclusiveReadResource() {
        return configuration.get(READ_SUFFIX, s -> Arrays.stream(s.split(","))
                .map(String::trim))
                .orElse(Stream.empty());
    }

    Stream<ExclusiveResource> getExclusiveResources() {
        Stream<ExclusiveResource> readWrite = exclusiveReadWriteResource()
                .map(resource -> new ExclusiveResource(resource, READ_WRITE));
        Stream<ExclusiveResource> read = exclusiveReadResource()
                .map(resource -> new ExclusiveResource(resource, READ));
        return Stream.concat(readWrite, read);
    }

}
