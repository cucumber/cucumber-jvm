package io.cucumber.core.runner;

import io.cucumber.core.backend.SourceReference;
import io.cucumber.core.backend.StaticHookDefinition;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

final class CoreStaticHookDefinition {

    private final UUID id;
    private final StaticHookDefinition delegate;

    private CoreStaticHookDefinition(UUID id, StaticHookDefinition delegate) {
        this.id = requireNonNull(id);
        this.delegate = delegate;
    }

    static CoreStaticHookDefinition create(StaticHookDefinition hookDefinition, Supplier<UUID> uuidGenerator) {
        return new CoreStaticHookDefinition(uuidGenerator.get(), hookDefinition);
    }

    void execute() {
        delegate.execute();
    }

    StaticHookDefinition getDelegate() {
        return delegate;
    }

    String getLocation() {
        return delegate.getLocation();
    }

    UUID getId() {
        return id;
    }

    int getOrder() {
        return delegate.getOrder();
    }

    Optional<StaticHookDefinition.HookType> getHookType() {
        return delegate.getHookType();
    }

    Optional<String> getName() {
        return delegate.getName();
    }

    Optional<SourceReference> getDefinitionLocation() {
        return delegate.getSourceReference();
    }

}
