package io.cucumber.core.backend;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
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

    Options getOptions();

    final class Builder {
        private final Set<GlueDiscoverySelector> gluePaths = new LinkedHashSet<>();
        private Options options = new DefaultOptions();

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

        public Builder options(Options options) {
            this.options = options;
            return this;
        }

        public GlueDiscoveryRequest build() {
            return new DefaultGlueDiscoveryRequest(options, List.copyOf(gluePaths));
        }

        private static final class DefaultOptions implements Options {
            @Override
            public @Nullable Class<? extends ObjectFactory> getObjectFactoryClass() {
                return null;
            }

            @Override
            public boolean isGlueHintEnabled() {
                return false;
            }

            @Override
            public Duration getGlueHintThreshold() {
                return Duration.ZERO;
            }
        }
    }

    final class DefaultGlueDiscoveryRequest implements GlueDiscoveryRequest {
        private final Options options;
        private final List<GlueDiscoverySelector> gluePaths;

        public DefaultGlueDiscoveryRequest(Options options, List<GlueDiscoverySelector> gluePaths) {
            this.options = options;
            this.gluePaths = gluePaths;
        }

        @Override
        public <T extends GlueDiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType) {
            return gluePaths.stream().filter(selectorType::isInstance) //
                    .map(selectorType::cast) //
                    .toList();
        }

        @Override
        public Options getOptions() {
            return options;
        }
    }

}
