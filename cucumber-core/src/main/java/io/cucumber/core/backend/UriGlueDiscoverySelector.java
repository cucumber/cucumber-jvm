package io.cucumber.core.backend;

import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Objects;

public final class UriGlueDiscoverySelector implements GlueDiscoverySelectors {
    private final URI uri;

    UriGlueDiscoverySelector(URI uri) {
        this.uri = uri;
    }

    public URI uri() {
        return uri;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof UriGlueDiscoverySelector that))
            return false;
        return Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uri);
    }

    @Override
    public String toString() {
        return "UriGlueDiscoverySelector[" +
                "uri=" + uri + ']';
    }

}
