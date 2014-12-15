package cucumber.runtime.io;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Dummy URLStreamHandler that's just specified to suppress the standard
 * {@code java.net.URL} URLStreamHandler lookup, to be able to
 * use the standard URL class for parsing "rmi:..." URLs.
 */
class DummyURLStreamHandler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        throw new UnsupportedOperationException();
    }
}