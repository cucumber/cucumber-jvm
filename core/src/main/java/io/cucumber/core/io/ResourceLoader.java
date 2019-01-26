package io.cucumber.core.io;

import java.net.URI;

public interface ResourceLoader {
    Iterable<Resource> resources(URI path, String suffix);
}
