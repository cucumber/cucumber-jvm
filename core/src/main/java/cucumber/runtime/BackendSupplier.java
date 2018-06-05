package cucumber.runtime;

import java.util.Collection;

public interface BackendSupplier {
    Collection<? extends Backend> get();
}
