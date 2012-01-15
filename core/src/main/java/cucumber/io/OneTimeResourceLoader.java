package cucumber.io;

import java.util.HashSet;
import java.util.Set;

/**
 * A wrapper for the existing resource loaders to ensure that
 * they're only loaded one time.
 * This allows the other resource loaders to still be used, for whatever purpose they've got
 * whilst in special cases preventing things from being loaded multiple times.
 */
public class OneTimeResourceLoader implements ResourceLoader {

    private Set<String> loadedResourcePaths = new HashSet<String>();
    private ResourceLoader nestedLoader;

    public OneTimeResourceLoader(ResourceLoader loader) {
        this.nestedLoader = loader;
    }

    @Override
    public Iterable<Resource> resources(String path, String suffix) {
        Set<Resource> resourcesToLoad = new HashSet<Resource>();

        for (Resource resource : nestedLoader.resources(path, suffix)) {
            if (loadedResourcePaths.add(resource.getPath())) {
                resourcesToLoad.add(resource);
            }
        }

        return resourcesToLoad;
    }
}
