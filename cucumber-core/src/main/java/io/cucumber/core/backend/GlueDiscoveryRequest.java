package io.cucumber.core.backend;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A request for a {@link Backend} to discover {@link Glue} code in specific
 * location.
 */
public interface GlueDiscoveryRequest {

    static Builder builder() {
        return new Builder();
    }

    <T extends GlueDiscoverySelector> List<T> getSelectorsByType(Class<T> selector);

    final class Builder {
        private final Set<GlueDiscoverySelector> gluePaths = new LinkedHashSet<>();

        private Builder() {

        }

        public Builder selectors(GlueDiscoverySelector... selectors) {
            this.gluePaths.addAll(List.of(selectors));
            return this;
        }

        public Builder selectors(List<? extends GlueDiscoverySelector> selectors) {
            this.gluePaths.addAll(selectors);
            return this;
        }

        public GlueDiscoveryRequest build() {
            return new DefaultGlueDiscoveryRequest(List.copyOf(gluePaths));
        }
    }

    final class DefaultGlueDiscoveryRequest implements GlueDiscoveryRequest {
        private final List<GlueDiscoverySelector> gluePaths;

        public DefaultGlueDiscoveryRequest(List<GlueDiscoverySelector> gluePaths) {
            this.gluePaths = gluePaths;
        }

        @Override
        public <T extends GlueDiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType) {
            return gluePaths.stream().filter(selectorType::isInstance) //
                    .map(selectorType::cast) //
                    .toList();
        }
    }

}
