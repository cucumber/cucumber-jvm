package cucumber.runtime.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipResource implements Resource {
    private final ZipFile jarFile;
    private final ZipEntry jarEntry;

    public ZipResource(ZipFile jarFile, ZipEntry jarEntry) {
        this.jarFile = jarFile;
        this.jarEntry = jarEntry;
    }

    @Override
    public String getPath() {
        return jarEntry.getName();
    }

    @Override
    public String getAbsolutePath() {
        return jarFile.getName() + "!/" + getPath();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return jarFile.getInputStream(jarEntry);
    }

    @Override
    public String getClassName(String extension) {
        String path = getPath();
        return path.substring(0, path.length() - extension.length()).replace('/', '.');
    }
}
