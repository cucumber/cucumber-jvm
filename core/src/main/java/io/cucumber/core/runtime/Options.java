package io.cucumber.core.runtime;

import java.net.URI;
import java.util.List;

public interface Options {
    List<URI> getFeaturePaths();
}
