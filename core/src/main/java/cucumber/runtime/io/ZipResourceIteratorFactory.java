package cucumber.runtime.io;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;

import cucumber.runtime.CucumberException;

/**
 * Factory which creates {@link ZipResourceIterator}s for URL's with the jar
 * protocols ("jar", "zip", "wsjar" and "vfszip")
 */
public class ZipResourceIteratorFactory implements ResourceIteratorFactory {
    static String filePath(URL jarUrl) throws UnsupportedEncodingException, MalformedURLException {
        String path = new File(new URL(jarUrl.getFile()).getFile()).getAbsolutePath();
        String pathToJar = path.substring(0, path.lastIndexOf("!"));
        return URLDecoder.decode(pathToJar, "UTF-8");
    }

    @Override
    public boolean isFactoryFor(URL url) {
        return ResourceUtils.isJarURL(url);
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
