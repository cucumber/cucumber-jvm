package cucumber.runtime.android;

import android.content.Context;
import android.content.res.AssetManager;
import cucumber.runtime.io.Resource;

import java.io.IOException;
import java.io.InputStream;

class AndroidResource implements Resource {
    private final Context mContext;
    private final String mPath;

    public AndroidResource(Context context, String path) {
        mContext = context;
        mPath = path;
    }

    @Override
    public String getPath() {
        return mPath;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return mContext.getAssets().open(mPath, AssetManager.ACCESS_UNKNOWN);
    }

    @Override
    public String getClassName() {
        return mPath.substring(mPath.lastIndexOf("/"));
    }
}
