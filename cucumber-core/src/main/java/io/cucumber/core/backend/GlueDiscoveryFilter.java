package io.cucumber.core.backend;

import java.util.regex.Pattern;

public interface GlueDiscoveryFilter {

    interface ClassNameFilter extends GlueDiscoveryFilter {

        boolean apply(String className);

        static ClassNameFilter includeClassNamePatterns(Pattern pattern) {
            return new IncludeClassNameFilter(pattern);
        }

        static ClassNameFilter excludeClassNamePatterns(Pattern pattern) {
            return new ExcludeClassNameFilter(pattern);
        }

    }

}
