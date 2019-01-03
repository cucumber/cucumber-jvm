package cucumber.runtime.formatter;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

class UTF8OutputStreamWriter extends OutputStreamWriter {
    UTF8OutputStreamWriter(OutputStream out) {
        super(out, Charset.forName("UTF-8"));
    }
}
