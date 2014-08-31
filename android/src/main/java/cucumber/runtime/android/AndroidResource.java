package cucumber.runtime.android;

import android.content.Context;
import android.content.res.AssetManager;
import cucumber.runtime.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Android specific implementation of {@link cucumber.runtime.io.Resource} which is apple
 * to create {@link java.io.InputStream}s for android assets.
 */
public class AndroidResource implements Resource {

    /**
     * The {@link android.content.Context} to get the {@link java.io.InputStream} from
     */
    private final Context context;

    /**
     * The path of the resource.
     */
    private final String path;

    /**
     * Creates a new instance for the given parameters.
     *
     * @param context the {@link android.content.Context} to create the {@link java.io.InputStream} from
     * @param path the path to the ressource
     */
    public AndroidResource(final Context context, final String path) {
        this.context = context;
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getAbsolutePath() {
        return getPath();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return context.getAssets().open(path, AssetManager.ACCESS_UNKNOWN);
    }

    @Override
    public String getClassName(final String extension) {
        return path.substring(0, path.length() - extension.length()).replace('/', '.');
    }

    @Override
    public String toString() {
        return "AndroidResource (" + path + ")";
    }
}
