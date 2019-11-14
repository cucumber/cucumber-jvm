package io.cucumber.core.resource;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface UriResourceScanner {

    <R> List<R> scanForResourcesUri(URI resourcePath, Function<Resource, Optional<R>> loadResource);

}
