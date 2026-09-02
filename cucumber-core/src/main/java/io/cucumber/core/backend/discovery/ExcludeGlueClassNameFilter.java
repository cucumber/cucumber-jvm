package io.cucumber.core.backend.discovery;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

final class ExcludeGlueClassNameFilter implements GlueClassNameFilter {
    private final List<Pattern> patterns;

    ExcludeGlueClassNameFilter(Pattern... patterns) {
        this.patterns = List.of(patterns);
    }

    @Override
    public boolean apply(String className) {
        return patterns.stream().noneMatch(pattern -> pattern.matcher(className).matches());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ExcludeGlueClassNameFilter that))
            return false;
        return Objects.equals(getPatterns(), that.getPatterns());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getPatterns());
    }

    private List<String> getPatterns() {
        // Pattern doesn't implement equals/hashcode
        return patterns.stream().map(Pattern::pattern).toList();
    }

    @Override
    public String toString() {
        return "ExcludeClassNameFilter{" +
                "patterns=" + getPatterns() +
                '}';
    }
}
