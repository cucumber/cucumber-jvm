package io.cucumber.core.backend.discovery;

import org.apiguardian.api.API;

import java.util.regex.Pattern;

/**
 * A filter that defines what glue backends should include or exclude.
 */
@API(status = API.Status.EXPERIMENTAL, since = "8.0.0")
public interface GlueDiscoveryFilter {

    static GlueClassNameFilter includeClassNamePatterns(Pattern... pattern) {
        return new IncludeGlueClassNameFilter(pattern);
    }

    static GlueClassNameFilter excludeClassNamePatterns(Pattern... pattern) {
        return new ExcludeGlueClassNameFilter(pattern);
    }

}
