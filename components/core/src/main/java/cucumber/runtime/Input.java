package cucumber.runtime;

import java.io.InputStream;
import java.io.Reader;

public interface Input {
    String getPath();

    InputStream getInputStream();

    Reader getReader();

    String getString();
}
