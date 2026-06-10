package io.cucumber.core.backend;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A request for a {@link Backend} to discover {@link Glue} code in specific
 * location.
 */
public interface GlueDiscoveryRequest {
    List<URI> getGluePaths();

    static Builder builder() {
        return new Builder();
    }

    final class Builder {
        private final Set<URI> gluePaths = new LinkedHashSet<>();

        private Builder() {

        }

        public Builder gluePath(URI... gluePath) {
            this.gluePaths.addAll(List.of(gluePath));
            return this;
        }

        public GlueDiscoveryRequest build() {
            return new DefaultGlueDiscoveryRequest(List.copyOf(gluePaths));
        }
    }

    final class DefaultGlueDiscoveryRequest implements GlueDiscoveryRequest {
        private final List<URI> gluePaths;

        public DefaultGlueDiscoveryRequest(List<URI> gluePaths) {
            this.gluePaths = gluePaths;
        }

        @Override
        public List<URI> getGluePaths() {
            return gluePaths;
        }
    }

}
