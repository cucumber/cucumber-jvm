package cucumber.io;

import cucumber.runtime.CucumberException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Iterator;

class ClasspathIterable implements Iterable<Resource> {
    private final ClassLoader cl;
    private final ResourceIteratorFactory fallbackResourceIteratorFactory;
    private final String path;
    private final String suffix;

    public ClasspathIterable(ClassLoader cl, String path, String suffix) {
        this.cl = cl;
        this.fallbackResourceIteratorFactory = new FileResourceIteratorFactory();
        this.path = path;
        this.suffix = suffix;
    }

    @Override
    public Iterator<Resource> iterator() {
        try {
            FlatteningIterator iterator = new FlatteningIterator();
            Enumeration<URL> resources = cl.getResources(path);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (url.getProtocol().equals("jar")) {
                    String jarPath = filePath(url);
                    iterator.push(new ZipResourceIterator(jarPath, path, suffix));
                } else {
                    iterator.push(fallbackResourceIteratorFactory.createIterator(url, path, suffix));
                }
            }
            return iterator;
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    static String filePath(URL jarUrl) throws UnsupportedEncodingException, MalformedURLException {
        String path = new File(new URL(jarUrl.getFile()).getFile()).getAbsolutePath();
        String pathToJar = path.substring(0, path.indexOf("!"));
        return URLDecoder.decode(pathToJar, "UTF-8");
    }

    static boolean hasSuffix(String suffix, String name) {
        return suffix == null || name.endsWith(suffix);
    }

    static String getPath(URL url) {
        try {
            return URLDecoder.decode(url.getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CucumberException("Encoding problem", e);
        }
    }
}
