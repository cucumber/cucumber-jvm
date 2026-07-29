package io.cucumber.core.backend;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static io.cucumber.core.backend.GlueDiscoveryFilter.ClassNameFilter.excludeClassNamePatterns;
import static io.cucumber.core.backend.GlueDiscoveryFilter.ClassNameFilter.includeClassNamePatterns;
import static org.assertj.core.api.Assertions.assertThat;

class ClassNameFilterTest {

    @Test
    void includes() {
         var include = includeClassNamePatterns(Pattern.compile("A"));
        assertThat(include.apply("A")).isTrue();
        assertThat(include.apply("B")).isFalse();
    }

    @Test
    void includesIfAnyOneMatches() {
         var include = includeClassNamePatterns(
                 Pattern.compile("A"),
                 Pattern.compile("B")
         );
        assertThat(include.apply("A")).isTrue();
        assertThat(include.apply("B")).isTrue();
        assertThat(include.apply("C")).isFalse();
    }

    @Test
    void excludes() {
         var exclude = excludeClassNamePatterns(Pattern.compile("A"));
        assertThat(exclude.apply("A")).isFalse();
        assertThat(exclude.apply("B")).isTrue();
    }

    @Test
    void excludesIfAnyOneMatches() {
        var exclude = excludeClassNamePatterns(
                Pattern.compile("A"),
                Pattern.compile("B")
        );
        assertThat(exclude.apply("A")).isFalse();
        assertThat(exclude.apply("B")).isFalse();
        assertThat(exclude.apply("C")).isTrue();
    }
}
