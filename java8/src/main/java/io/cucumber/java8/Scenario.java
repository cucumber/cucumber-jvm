package io.cucumber.java8;

import io.cucumber.core.backend.TestCaseState;
import org.apiguardian.api.API;

import java.net.URI;
import java.util.Collection;

/**
 * Before or After Hooks that declare a parameter of this type will receive an instance of this class.
 * It allows writing text and embedding media into reports, as well as inspecting results (in an After block).
 * <p>
 * Note: This class is not intended to be used to create reports. To create custom reports use
 * the {@code io.cucumber.plugin.Plugin} class. The plugin system provides a much richer access to Cucumbers then
 * hooks after could provide. For an example see {@code io.cucumber.core.plugin.PrettyFormatter}.
 */
@API(status = API.Status.STABLE)
public final class Scenario {

    private final TestCaseState delegate;

    Scenario(TestCaseState delegate) {
        this.delegate = delegate;
    }

    public Collection<String> getSourceTagNames() {
        return delegate.getSourceTagNames();
    }

    public Status getStatus() {
        return Status.valueOf(delegate.getStatus().name());
    }

    public boolean isFailed() {
        return delegate.isFailed();
    }

    @Deprecated
    public void embed(byte[] data, String mediaType) {
        delegate.embed(data, mediaType);
    }

    public void embed(byte[] data, String mediaType, String name) {
        delegate.embed(data, mediaType, name);
    }

    public void write(String text) {
        delegate.write(text);
    }

    public String getName() {
        return delegate.getName();
    }

    public String getId() {
        return delegate.getId();
    }

    public URI getUri() {
        return delegate.getUri();
    }

    public Integer getLine() {
        return delegate.getLine();
    }
}
