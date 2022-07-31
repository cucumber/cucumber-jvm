package io.cucumber.core.resource;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

class CloseablePath implements Closeable {

    private static final Closeable NULL_CLOSEABLE = () -> {
    };

    private final Path path;
    private final Closeable delegate;

    private CloseablePath(Path path, Closeable delegate) {
        this.path = path;
        this.delegate = delegate;
    }

    static CloseablePath open(URI uri) {
        return CloseablePath.open(Paths.get(uri), NULL_CLOSEABLE);
    }

    static CloseablePath open(Path path, Closeable o) {
        return new CloseablePath(path, o);
    }

    Path getPath() {
        return path;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

}
