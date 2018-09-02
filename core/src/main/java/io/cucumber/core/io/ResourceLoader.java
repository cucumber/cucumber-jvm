package io.cucumber.core.io;

public interface ResourceLoader {
    Iterable<Resource> resources(String path, String suffix);
}
