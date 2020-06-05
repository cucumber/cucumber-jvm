package io.cucumber.core.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Minimal representation of a resource e.g. a feature file.
 */
public interface Resource {

    /**
     * Returns a uri representing this resource.
     * <p>
     * Resources on the classpath will have the form
     * {@code classpath:com/example.feature} while resources on the file system
     * will have the form {@code file:/path/to/example.feature}. Other resources
     * will be represented by their exact uri.
     *
     * @return a uri representing this resource
     */
    URI getUri();

    InputStream getInputStream() throws IOException;

}
