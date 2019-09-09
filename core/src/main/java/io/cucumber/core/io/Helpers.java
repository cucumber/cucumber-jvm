package io.cucumber.core.io;

import io.cucumber.core.exception.CucumberException;

import java.net.URI;

final class Helpers {
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
