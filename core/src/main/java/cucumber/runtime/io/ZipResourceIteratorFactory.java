package cucumber.runtime.io;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import cucumber.runtime.CucumberException;

/**
 * Factory which creates {@link ZipResourceIterator}s for URL's with "jar", "zip" and "wsjar"
 * protocols.
 */
public class ZipResourceIteratorFactory implements ResourceIteratorFactory {

    // weblogic uses zip as protocol for classpath resources inside jars,and webshpere uses wsjar, so
    // those protocols must be handled too
    private static final Set<String> SUPPORTED_PROTOCOLS
        = new HashSet<String>(Arrays.asList("jar", "zip", "wsjar"));

    static String filePath(URL jarUrl) throws UnsupportedEncodingException, MalformedURLException {
        String urlFile = jarUrl.getFile();
        if (urlFile.startsWith("file:")) {
            urlFile = urlFile.substring("file:".length());
        }

        int separatorIndex = urlFile.indexOf("!/");
        if (separatorIndex != -1) {
            urlFile = urlFile.substring(0, separatorIndex);
        }

        return URLDecoder.decode(urlFile, "UTF-8");
    }

    @Override
    public boolean isFactoryFor(URL url) {
        return SUPPORTED_PROTOCOLS.contains(url.getProtocol().toLowerCase());
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
