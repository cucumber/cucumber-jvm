package cucumber.runtime.io;

import cucumber.runtime.CucumberException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;

/**
 * Factory which creates {@link ZipResourceIterator}s for URL's with the "jar"
 * protocol.
 */
public class ZipResourceIteratorFactory implements ResourceIteratorFactory {
    static String filePath(URL jarUrl) throws UnsupportedEncodingException, MalformedURLException {
        String path = new File(new URL(jarUrl.getFile()).getFile()).getAbsolutePath();
        String pathToJar = path.substring(0, path.lastIndexOf("!"));
        return URLDecoder.decode(pathToJar, "UTF-8");
    }

    @Override
    public boolean isFactoryFor(URL url) {
        return "jar".equals(url.getProtocol());
    }

    @Override
    public Iterator<Resource> createIterator(URL url, String path, String suffix) {
        try {
            String jarPath = filePath(url);
            return new ZipResourceIterator(jarPath, path, suffix);
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }
}
