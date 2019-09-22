package io.cucumber.core.io;

import java.net.URI;
import java.util.Iterator;

/**
 * Factory contract for creating resource iterators.
 */
interface ResourceIteratorFactory {

    /**
     * Gets a value indicating whether the factory can create iterators for the
     * resource specified by the given URL.
     *
     * @param url The URL to check.
     * @return True if the factory can create an iterator for the given URL.
     */
    boolean isFactoryFor(URI url);

    /**
     * Creates an iterator for the given URL with the path and suffix.
     *
     * @param url    The URL.
     * @param path   The path.
     * @param suffix The suffix.
     * @return The iterator over the resources designated by the URL, path, and
     * suffix.
     */
    Iterator<Resource> createIterator(URI url, String path, String suffix);
}
