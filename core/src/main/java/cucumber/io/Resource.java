package cucumber.io;

import java.io.IOException;
import java.io.InputStream;

public interface Resource {
    String getPath();

    InputStream getInputStream() throws IOException;

    String getClassName();
}
