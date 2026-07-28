package io.cucumber.core.backend;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class ClassGlueDiscoverySelector implements GlueDiscoverySelectors {
    private final String name;

    ClassGlueDiscoverySelector(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof ClassGlueDiscoverySelector that)) return false;
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
