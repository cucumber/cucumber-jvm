package io.cucumber.core.backend.discovery;

import org.apiguardian.api.API;

/**
 * Filters glue classes by name.
 */
@API(status = API.Status.EXPERIMENTAL, since = "8.0.0")
public interface GlueClassNameFilter extends GlueDiscoveryFilter {

    boolean apply(String className);

}
