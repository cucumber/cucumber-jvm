package cucumber.runtime.io;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import cucumber.runtime.CucumberException;

/**
 * Factory which creates {@link ZipResourceIterator}s for URL's with "jar", "zip" and "wsjar"
 * protocols.
 */
public class ZipResourceIteratorFactory implements ResourceIteratorFactory {

    static String filePath(URL jarUrl) throws UnsupportedEncodingException, MalformedURLException {
        String urlFile = jarUrl.getFile();

        int separatorIndex = urlFile.indexOf("!/");
        if (separatorIndex != -1) {
            urlFile = urlFile.substring(0, separatorIndex);
        }

        URL url = new URL(urlFile);
        return new File(url.getFile()).getPath();
    }

    @Override
    public boolean isFactoryFor(URL url) {
        return url.getFile().indexOf("!/") != -1;
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
