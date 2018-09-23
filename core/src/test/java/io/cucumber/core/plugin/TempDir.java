package io.cucumber.core.plugin;

import java.io.File;
import java.io.IOException;

class TempDir {
    private TempDir() {
    }

    static File createTempDirectory() throws IOException {
        File temp = createTempFile();

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        temp.deleteOnExit();

        return temp;
    }

    static File createTempFile() throws IOException {
        return File.createTempFile("temp", Long.toString(System.nanoTime()));
    }
}
