package cucumber.runtime.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class UTF8OutputStreamWriter extends OutputStreamWriter {
    public UTF8OutputStreamWriter(OutputStream out) throws IOException {
        super(out, Charset.forName("UTF-8"));
    }
}
