package io.cucumber.core.resource;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface UriResourceScanner {

    <R> List<R> scanForResourcesPath(Path resourcePath, Function<Resource, Optional<R>> loadResource);

}
