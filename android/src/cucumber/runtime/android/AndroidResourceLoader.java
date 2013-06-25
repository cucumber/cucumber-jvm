package cucumber.runtime.android;

import android.content.Context;
import android.content.res.AssetManager;
import cucumber.runtime.CucumberException;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AndroidResourceLoader implements ResourceLoader {
    private Context context;

    public AndroidResourceLoader(Context context) {
        this.context = context;
    }

    @Override
    public Iterable<Resource> resources(String path, String suffix) {
        try {
            List<Resource> resources = new ArrayList<Resource>();
            AssetManager assetManager = context.getAssets();
            addResourceRecursive(resources, assetManager, path, suffix);
            return resources;
        } catch (IOException e) {
            throw new CucumberException("Error loading resources.", e);
        }
    }

    private void addResourceRecursive(List<Resource> res, AssetManager am, String path, String suffix) throws IOException {
        if (path.endsWith(suffix)) {
            res.add(new AndroidResource(context, path));
        } else {
            for (String name : am.list(path)) {
                if (name.endsWith(suffix)) {
                    Resource as = new AndroidResource(context, String.format("%s/%s", path, name));
                    res.add(as);
                } else {
                    addResourceRecursive(res, am, String.format("%s/%s", path, name), suffix);
                }
            }
        }
    }
}
