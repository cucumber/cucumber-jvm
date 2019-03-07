package io.cucumber.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static io.cucumber.core.io.MultiLoader.FILE_SCHEME;
import static io.cucumber.core.model.Classpath.CLASSPATH_SCHEME;


final class FileResource implements Resource {
    private final File root;
    private final File file;
    private final boolean classpathFileResource;

    static FileResource createFileResource(File root, File file) {
        return new FileResource(root, file, false);
    }

    static FileResource createClasspathFileResource(File classpathRoot, File file) {
        return new FileResource(classpathRoot, file, true);
    }

    private FileResource(File root, File file, boolean classpathFileResource) {
        this.root = root;
        this.file = file;
        this.classpathFileResource = classpathFileResource;
        if (!file.getAbsolutePath().startsWith(root.getAbsolutePath())) {
            throw new IllegalArgumentException(file.getAbsolutePath() + " is not a parent of " + root.getAbsolutePath());
        }
    }

    @Override
    public URI getPath() {
        if (classpathFileResource) {
            return createURI(CLASSPATH_SCHEME, getRelativePath());
        } else if (root.equals(file)) {
            return file.toURI();
        } else {
            return createURI(FILE_SCHEME, getRelativePath());
        }
    }

    private static URI createURI(String classpathScheme, String ssp) {
        try {
            return new URI(classpathScheme, ssp, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String getRelativePath() {
        URI relativeUri = root.toURI().relativize(file.toURI());
        return relativeUri.getSchemeSpecificPart();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

}
