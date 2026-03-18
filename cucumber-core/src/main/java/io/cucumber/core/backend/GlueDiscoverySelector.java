package io.cucumber.core.backend;

import java.net.URI;
import java.util.Objects;

public interface GlueDiscoverySelector {

    class UriGlueDiscoverySelector implements GlueDiscoverySelector {

        private final URI uri;

        public UriGlueDiscoverySelector(URI uri) {
            this.uri = Objects.requireNonNull(uri);
        }

        public URI getUri() {
            return uri;
        }
    }

    class ClassGlueDiscoverySelector implements GlueDiscoverySelector {

        private final String className;

        public ClassGlueDiscoverySelector(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

}
