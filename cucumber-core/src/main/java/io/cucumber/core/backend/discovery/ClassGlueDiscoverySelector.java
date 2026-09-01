package io.cucumber.core.backend.discovery;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Select a specific class as glue.
 */
@API(status = API.Status.EXPERIMENTAL, since = "8.0.0")
public final class ClassGlueDiscoverySelector implements GlueDiscoverySelector {
    private final String name;

    ClassGlueDiscoverySelector(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof ClassGlueDiscoverySelector that))
            return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "ClassGlueDiscoverySelector[" +
                "name=" + name + ']';
    }

}
