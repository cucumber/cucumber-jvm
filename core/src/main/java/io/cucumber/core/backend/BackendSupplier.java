package io.cucumber.core.backend;

import java.util.Collection;

public interface BackendSupplier {
    Collection<? extends Backend> get();
}
