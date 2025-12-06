package io.cucumber.picocontainer;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Snippet;

import java.net.URI;
import java.util.List;

final class PicoBackend implements Backend {

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
    }

    @Override
    public void buildWorld() {
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public Snippet getSnippet() {
        return null;
    }

}
