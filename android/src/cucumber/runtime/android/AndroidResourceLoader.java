package cucumber.runtime.android;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import cucumber.api.android.CucumberInstrumentation;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AndroidResourceLoader implements ResourceLoader {
    private Context mContext;

    public AndroidResourceLoader(Context context) {
        mContext = context;
    }

    @Override
    public Iterable<Resource> resources(String path, String suffix) {
        List<Resource> resources = new ArrayList<Resource>();
        AssetManager assetManager = mContext.getAssets();
        try {
            addResourceRecursive(resources, assetManager, path, suffix);
        } catch (IOException e) {
            Log.e(CucumberInstrumentation.TAG, "Error loading resources.", e);
        }
        return resources;
    }

    private void addResourceRecursive(List<Resource> res, AssetManager am, String path, String suffix) throws IOException {
        if (path.endsWith(suffix)) {
            res.add(new AndroidResource(mContext, path));
        } else {
            for (String name : am.list(path)) {
                if (name.endsWith(suffix)) {
                    Resource as = new AndroidResource(mContext, String.format("%s/%s", path, name));
                    res.add(as);
                } else {
                    addResourceRecursive(res, am, String.format("%s/%s", path, name), suffix);
                }
            }
        }
    }
}
