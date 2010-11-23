package cucumber.runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface Input {
    String getPath();
    InputStream stream() throws IOException;
}
