package io.cucumber.core.backend;

import java.net.URI;

public interface GlueDiscoverySelector {

    static UriGlueDiscoverySelector selectUri(URI uri) {
        return new UriGlueDiscoverySelector(uri);
    }

    static UriGlueDiscoverySelector selectUri(String uri) {
        return selectUri(URI.create(uri));
    }

    record UriGlueDiscoverySelector(URI uri) implements GlueDiscoverySelector {

    }

}
