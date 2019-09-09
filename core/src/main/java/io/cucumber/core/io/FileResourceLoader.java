package io.cucumber.core.io;

import java.io.File;
import java.net.URI;

import static io.cucumber.core.io.MultiLoader.FILE_SCHEME;

final class FileResourceLoader implements ResourceLoader {
    @Override
    public Iterable<Resource> resources(URI path, String suffix) {
        if(!FILE_SCHEME.equals(path.getScheme())){
            throw new IllegalArgumentException("path must have file scheme " + path);
        }

        File file = new File(path.getSchemeSpecificPart());
        if (file.isAbsolute()) {
            return new FileResourceIterable(file, file, suffix);
        } else {
            File root = new File("");
            return new FileResourceIterable(root, file, suffix);
        }
    }
}
