package cucumber.runtime;

import java.io.IOException;

public interface Consumer {
    public void consume(Input input) throws IOException;
}
