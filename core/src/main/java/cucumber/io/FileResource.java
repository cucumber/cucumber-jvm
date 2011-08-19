package cucumber.io;

import cucumber.runtime.CucumberException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileResource extends AbstractResource {
    private final File rootDir;
    private final File file;

    public FileResource(File rootDir, File file) {
        this.rootDir = rootDir;
        this.file = file;
    }

    public String getPath() {
        return file.getAbsolutePath().substring(rootDir.getAbsolutePath().length() + 1, file.getAbsolutePath().length()).replace(File.separatorChar, '/');
    }

    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new CucumberException("Failed to read from file " + file.getAbsolutePath(), e);
        }
    }
}
