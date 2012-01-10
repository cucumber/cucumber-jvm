package cucumber.resources;

import cucumber.runtime.CucumberException;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipResource extends AbstractResource {
    private final ZipFile jarFile;
    private final ZipEntry jarEntry;

    public ZipResource(ZipFile jarFile, ZipEntry jarEntry, PathWithLines pwl) {
        super(pwl);
        this.jarFile = jarFile;
        this.jarEntry = jarEntry;
    }

    public String getPath() {
        return jarEntry.getName();
    }

    public InputStream getInputStream() {
        try {
            return jarFile.getInputStream(jarEntry);
        } catch (IOException e) {
            throw new CucumberException("Failed to read from jar file", e);
        }
    }
}
