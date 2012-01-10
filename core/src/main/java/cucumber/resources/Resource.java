package cucumber.resources;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;

public interface Resource {
    String getPath();

    InputStream getInputStream();

    Reader getReader();

    String getString();

    List<Long> getLines();
}
