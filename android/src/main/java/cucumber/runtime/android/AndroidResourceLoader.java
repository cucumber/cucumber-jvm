package cucumber.runtime.android;

import android.content.Context;
import android.content.res.AssetManager;
import cucumber.runtime.CucumberException;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Android specific implementation of {@link cucumber.runtime.io.ResourceLoader} which loads non-class resources such as .feature files.
 */
public class AndroidResourceLoader implements ResourceLoader {

    /**
     * The format of the resource path.
     */
    public static final String RESOURCE_PATH_FORMAT = "%s/%s";

    /**
     * The {@link android.content.Context} to get the resources from.
     */
    private final Context context;

    /**
     * Creates a new instance for the given parameter.
     *
     * @param context the {@link android.content.Context} to get resources from
     */
    public AndroidResourceLoader(final Context context) {
        this.context = context;
    }

    @Override
    public Iterable<Resource> resources(final String path, final String suffix) {
        try {
            final List<Resource> resources = new ArrayList<Resource>();
            final AssetManager assetManager = context.getAssets();
            addResourceRecursive(resources, assetManager, path, suffix);
            return resources;
        } catch (final IOException e) {
            throw new CucumberException("Error loading resources from " + path + " with suffix " + suffix, e);
        }
    }

    private void addResourceRecursive(final List<Resource> resources,
                                      final AssetManager assetManager,
                                      final String path,
                                      final String suffix) throws IOException {
        if (path.endsWith(suffix)) {
            resources.add(new AndroidResource(context, path));
            return;
        }

        for (final String name : assetManager.list(path)) {
            addResourceRecursive(resources, assetManager, String.format(RESOURCE_PATH_FORMAT, path, name), suffix);
        }
    }
}
