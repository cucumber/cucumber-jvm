package io.cucumber.core.backend;

import io.cucumber.core.exception.CucumberException;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface ConfigurationParameters {

    Optional<String> get(String key);

    default <T> Optional<T> get(String key, Function<? super String, ? extends @Nullable T> transformer) {
        requireNonNull(transformer);
        Function<String, T> safeTransformer = (input) -> {
            try {
                return transformer.apply(input);
            } catch (Exception e) {
                throw new CucumberException("Could not transform configuration parameter '%s' and value '%s'".formatted(key, input), e);
            }
        };
        return get(key).map(safeTransformer);
    }
}
