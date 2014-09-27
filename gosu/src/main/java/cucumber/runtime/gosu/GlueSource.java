package cucumber.runtime.gosu;

import cucumber.runtime.io.Resource;
import gw.lang.launch.IArgInfo;
import gw.lang.launch.IBooleanArgKey;
import gw.lang.launch.IProgramSource;
import gw.lang.launch.IStringArgKey;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

class GlueSource implements IProgramSource {
    private final StringBuilder sourceBuilder = new StringBuilder();

    @Override
    public String getRawPath() {
        // This file doesn't really exist, but we have to say something I guess!
        return "cucumber.gsp";
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(sourceBuilder.toString().getBytes("UTF-8"));
    }

    public void addGlueScript(Resource glueScript) {
        // https://groups.google.com/d/msg/gosu-lang/yMJnzQwuFpo/msg81GNGlAYJ
        String className = glueScript.getClassName(".gsp");
        sourceBuilder.append("(" + className + ".Type as java.lang.Class).getDeclaredMethod( \"evaluate\", {gw.lang.reflect.gs.IExternalSymbolMap} ).invoke( new " + className + "(), {null})\n");
    }

    public IArgInfo toArgInfo() {
        return new IArgInfo() {
            @Override
            public boolean consumeArg(IBooleanArgKey iBooleanArgKey) {
                return false;
            }

            @Override
            public String consumeArg(IStringArgKey iStringArgKey) {
                return null;
            }

            @Override
            public void processUnknownArgs() {

            }

            @Override
            public String getErrorMessage() {
                return null;
            }

            @Override
            public IProgramSource getProgramSource() {
                return GlueSource.this;
            }

            @Override
            public List<String> getArgsList() {
                return null;
            }
        };
    }
}
