package io.cucumber.core.io;

import io.cucumber.core.exception.CucumberException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

final class Helpers {
    private Helpers() {
    }

    static boolean hasSuffix(String suffix, String name) {
        return suffix == null || name.endsWith(suffix);
    }

    static String filePath(URL fileUrl) {
        if (!"file".equals(fileUrl.getProtocol())) {
            throw new CucumberException("Expected a file URL:" + fileUrl.toExternalForm());
        }
        try {
            return fileUrl.toURI().getSchemeSpecificPart();
        } catch (URISyntaxException e) {
            throw new CucumberException(e);
        }
    }

    static String jarFilePath(URL jarUrl) {
        String urlFile = jarUrl.getFile();

        int separatorIndex = urlFile.indexOf("!/");
        if (separatorIndex == -1) {
            throw new CucumberException("Expected a jar URL: " + jarUrl.toExternalForm());
        }
        try {
            URL fileUrl = new URL(urlFile.substring(0, separatorIndex));
            return filePath(fileUrl);
        } catch (MalformedURLException e) {
            throw new CucumberException(e);
        }
    }
}
