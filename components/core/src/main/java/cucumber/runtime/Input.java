package cucumber.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public interface Input {
    String getPath();
    InputStream getInputStream() throws IOException;
    Reader getReader() throws IOException;
    String getString() throws IOException;
}
