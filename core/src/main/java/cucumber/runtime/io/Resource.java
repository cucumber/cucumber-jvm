package cucumber.runtime.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public interface Resource {
    URI getPath();

    InputStream getInputStream() throws IOException;

}
