package io.cucumber.core.backend;

import java.net.URI;
import java.util.List;

/**
 * A request to a Backend implementation to discover glue classes.
 */
public interface GlueDiscoveryRequest {

    /**
     * Returns a list of (typically classpath) URIs for glue discovery.
     *
     * @return a list of (typically classpath) URIs for glue discovery.
     */
    List<URI> getGlue();

    /**
     * Returns a list class names to consider for glue discovery.
     *
     * @return a list class names to consider for glue discovery.
     */
    List<String> getGlueClassNames();

}
