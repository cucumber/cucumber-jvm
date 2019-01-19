package cucumber.runtime.io;

import cucumber.runtime.CucumberException;

import java.net.URI;

class Helpers {
    private Helpers() {
    }

    static boolean hasSuffix(String suffix, String name) {
        return suffix == null || name.endsWith(suffix);
    }

    static URI jarFilePath(URI jarUrl) {
        String urlFile = jarUrl.getRawSchemeSpecificPart();

        int separatorIndex = urlFile.indexOf("!/");
        if (separatorIndex == -1) {
            throw new CucumberException("Expected a jar URL: " + jarUrl);
        }
        return URI.create(urlFile.substring(0, separatorIndex));
    }
}
