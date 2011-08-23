package cucumber.resources;

import cucumber.runtime.CucumberException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileResource extends AbstractResource {
    private final File rootDir;
    private final File file;

    public FileResource(File rootDir, PathWithLines pathWithLines) {
        super(pathWithLines);
        this.rootDir = rootDir;
        this.file = new File(pathWithLines.path);
    }

    public String getPath() {
        //return pathWithLines.path;
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
