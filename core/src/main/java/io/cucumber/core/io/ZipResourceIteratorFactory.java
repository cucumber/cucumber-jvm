package io.cucumber.core.io;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import io.cucumber.core.exception.CucumberException;

/**
 * Factory which creates {@link ZipResourceIterator}s for URL's with "jar", "zip" and "wsjar"
 * protocols.
 */
final class ZipResourceIteratorFactory implements ResourceIteratorFactory {

    @Override
    public boolean isFactoryFor(URI url) {
        return url.getSchemeSpecificPart().contains("!/");
    }

    @Override
    public Iterator<Resource> createIterator(URI url, String path, String suffix) {
        try {
            URI fileUri = Helpers.jarFilePath(url);
            return new ZipResourceIterator(fileUri, path, suffix);
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }
}
