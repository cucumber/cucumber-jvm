package io.cucumber.core.runtime;

import io.cucumber.core.backend.Backend;

import java.util.Collection;

public interface BackendSupplier {

    Collection<? extends Backend> get();

}
