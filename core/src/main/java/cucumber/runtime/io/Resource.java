package cucumber.runtime.io;

import java.io.IOException;
import java.io.InputStream;

public interface Resource {
    String getPath();

    String getAbsolutePath();

    InputStream getInputStream() throws IOException;

    String getClassName(String extension);
}
