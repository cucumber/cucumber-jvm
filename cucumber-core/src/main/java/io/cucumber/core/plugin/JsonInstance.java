package io.cucumber.core.plugin;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.messages.ndjson.Json;
import io.cucumber.messages.ndjson.JsonProvider;
import io.cucumber.messages.ndjson.Serializer;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

final class JsonInstance {

    private static volatile @Nullable Json INSTANCE;

    private JsonInstance() {
        /* no-op */
    }

    private static Json instance() {
        if (INSTANCE == null) {
            synchronized (JsonInstance.class) {
                if (INSTANCE == null) {
                    var providers = ServiceLoader.load(JsonProvider.class).stream()
                            .map(ServiceLoader.Provider::get)
                            .toList();

                    INSTANCE = providers.stream()
                            .map(JsonProvider::instance)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .findFirst()
                            .orElseThrow(() -> createMissingDependenciesException(providers));
                }
            }
        }
        return requireNonNull(INSTANCE);

    }

    static <T> Serializer<T> serializer(Class<T> type) {
        return instance().serializer(type);
    }

    private static CucumberException createMissingDependenciesException(List<JsonProvider> providers) {
        return new CucumberException(
            """
                    Cucumber needs a JSON library to write reports.

                    Please ensure one of following dependencies is available.

                    %s

                    If you can't use of any of these libraries, but can use another well known library please create an issue.
                    """
                    .formatted(describeProviders(providers)));
    }

    private static String describeProviders(List<JsonProvider> providers) {
        return providers.stream()
                .map(provider -> """
                        %s, which requires
                        %s
                        """.formatted(provider.name(), describeDependencies(provider)))
                .collect(Collectors.joining("\n"));
    }

    private static String describeDependencies(JsonProvider jsonProvider) {
        return jsonProvider.dependencies().stream()
                .map(dependency -> "- %s from %s:%s"
                        .formatted(dependency.className(), dependency.groupId(), dependency.artifactId()))
                .collect(Collectors.joining("\n"));
    }
}
