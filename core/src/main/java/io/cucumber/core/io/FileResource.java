package io.cucumber.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

final class FileResource implements Resource {
    private final File root;
    private final File file;
    private final boolean classpathFileResource;

    static FileResource createFileResource(File root, File file) {
        return new FileResource(root, file, false);
    }

    static FileResource createClasspathFileResource(File root, File file) {
        return new FileResource(root, file, true);
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
    public String getPath() {
        if (classpathFileResource) {
            String relativePath = file.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);
            return relativePath.replace(File.separatorChar, '/');
        } else {
            return file.getPath();
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

}
