package io.cucumber.core.backend.discovery;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A selector for specific uris.
 */
@API(status = API.Status.EXPERIMENTAL, since = "8.0.0")
public final class UriGlueDiscoverySelector implements GlueDiscoverySelector {
    private final URI uri;

    UriGlueDiscoverySelector(URI uri) {
        this.uri = requireNonNull(uri);
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
