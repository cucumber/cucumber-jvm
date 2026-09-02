package io.cucumber.core.backend.discovery;

import org.apiguardian.api.API;

import java.net.URI;

import static java.util.Objects.requireNonNull;

/**
 * A selector that defines where the backend should look for Glue classes. For
 * example URI, a class, or a package (not yet implemented).
 */
@API(status = API.Status.EXPERIMENTAL, since = "8.0.0")
public interface GlueDiscoverySelector {

    static ClassGlueDiscoverySelector selectClass(String name) {
        return new ClassGlueDiscoverySelector(name);
    }

    static UriGlueDiscoverySelector selectUri(URI uri) {
        return new UriGlueDiscoverySelector(uri);
    }

    static UriGlueDiscoverySelector selectUri(String uri) {
        return selectUri(URI.create(requireNonNull(uri)));
    }

}
