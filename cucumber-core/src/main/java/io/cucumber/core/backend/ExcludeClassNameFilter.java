package io.cucumber.core.backend;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

final class ExcludeClassNameFilter implements GlueDiscoveryFilter.ClassNameFilter {
    private final Pattern pattern;

    ExcludeClassNameFilter(Pattern pattern) {
        this.pattern = Objects.requireNonNull(pattern);
    }

    @Override
    public boolean apply(String className) {
        return !pattern.matcher(className).matches();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof ExcludeClassNameFilter that))
            return false;
        return Objects.equals(pattern.pattern(), that.pattern.pattern());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pattern.pattern());
    }

    @Override
    public String toString() {
        return "ExcludeClassNameFilter{" +
                "pattern=" + pattern +
                '}';
    }
}
