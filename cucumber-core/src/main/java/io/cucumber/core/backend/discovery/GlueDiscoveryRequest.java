package io.cucumber.core.backend.discovery;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.Options;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A request for a {@link Backend} to discover {@link Glue} code in specific
 * location.
 */
@API(status = API.Status.EXPERIMENTAL, since = "8.0.0")
public interface GlueDiscoveryRequest {

    static Builder builder() {
        return new Builder();
    }

    <T extends GlueDiscoverySelector> List<T> getSelectorsByType(Class<T> selector);

    <T extends GlueDiscoveryFilter> List<T> getFiltersByType(Class<T> filterType);

    Options getOptions();

    final class Builder {
        private final Set<GlueDiscoverySelector> selectors = new LinkedHashSet<>();
        private final Set<GlueDiscoveryFilter> filters = new LinkedHashSet<>();
        private Options options = new DefaultOptions();

        private Builder() {

        }

        public Builder selectors(GlueDiscoverySelector... selectors) {
            this.selectors.addAll(List.of(selectors));
            return this;
        }

        public Builder selectors(List<? extends GlueDiscoverySelector> selectors) {
            this.selectors.addAll(selectors);
            return this;
        }

        public Builder filters(GlueDiscoveryFilter... filters) {
            this.filters.addAll(List.of(filters));
            return this;
        }

        public Builder filters(List<? extends GlueDiscoveryFilter> filters) {
            this.filters.addAll(filters);
            return this;
        }

        public Builder options(Options options) {
            this.options = options;
            return this;
        }

        public GlueDiscoveryRequest build() {
            return new DefaultGlueDiscoveryRequest(options, List.copyOf(selectors), List.copyOf(filters));
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
        private final List<GlueDiscoverySelector> selectors;
        private final List<GlueDiscoveryFilter> filters;

        public DefaultGlueDiscoveryRequest(
                Options options, List<GlueDiscoverySelector> selectors, List<GlueDiscoveryFilter> filters
        ) {
            this.options = options;
            this.selectors = selectors;
            this.filters = filters;
        }

        @Override
        public <T extends GlueDiscoverySelector> List<T> getSelectorsByType(Class<T> selectorType) {
            return selectors.stream() //
                    .filter(selectorType::isInstance) //
                    .map(selectorType::cast) //
                    .toList();
        }

        @Override
        public <T extends GlueDiscoveryFilter> List<T> getFiltersByType(Class<T> filterType) {
            return filters.stream() //
                    .filter(filterType::isInstance) //
                    .map(filterType::cast) //
                    .toList();
        }

        @Override
        public Options getOptions() {
            return options;
        }
    }

}
