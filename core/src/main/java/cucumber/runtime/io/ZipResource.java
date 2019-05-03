package cucumber.runtime.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static io.cucumber.core.model.Classpath.CLASSPATH_SCHEME_PREFIX;

class ZipResource implements Resource {
    private final ZipFile jarFile;
    private final ZipEntry jarEntry;

    ZipResource(ZipFile jarFile, ZipEntry jarEntry) {
        this.jarFile = jarFile;
        this.jarEntry = jarEntry;
    }

    @Override
    public URI getPath() {
        return URI.create(CLASSPATH_SCHEME_PREFIX + jarEntry.getName());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return jarFile.getInputStream(jarEntry);
    }

}
