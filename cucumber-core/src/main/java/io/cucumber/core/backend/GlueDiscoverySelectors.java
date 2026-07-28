package io.cucumber.core.backend;

import java.net.URI;

public interface GlueDiscoverySelectors {

    static ClassGlueDiscoverySelector selectClass(String name) {
        return new ClassGlueDiscoverySelector(name);
    }

    static UriGlueDiscoverySelector selectUri(URI uri) {
        return new UriGlueDiscoverySelector(uri);
    }

    static UriGlueDiscoverySelector selectUri(String uri) {
        return selectUri(URI.create(uri));
    }

}
