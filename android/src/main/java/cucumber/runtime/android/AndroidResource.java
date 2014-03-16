package cucumber.runtime.android;

import android.content.Context;
import android.content.res.AssetManager;
import cucumber.runtime.io.Resource;

import java.io.IOException;
import java.io.InputStream;

class AndroidResource implements Resource {
    private final Context context;
    private final String path;

    public AndroidResource(Context context, String path) {
        this.context = context;
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return context.getAssets().open(path, AssetManager.ACCESS_UNKNOWN);
    }

    @Override
    public String getClassName(String extension) {
        return path.substring(0, path.length() - extension.length()).replace('/', '.');
    }

    @Override
    public String toString() {
        return "AndroidResource (" + path + ")";
    }
}
