package cucumber.runtime.io;

import cucumber.runtime.CucumberException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class Helpers {
    static boolean hasSuffix(String suffix, String name) {
        return suffix == null || name.endsWith(suffix);
    }

    static String filePath(URL url) {
        try {
            return url.toURI().getSchemeSpecificPart();
        } catch (URISyntaxException e) {
            throw new CucumberException(e);
        }
    }

    static String jarFilePath(URL jarUrl) throws UnsupportedEncodingException, MalformedURLException {
        String urlFile = jarUrl.getFile();

        int separatorIndex = urlFile.indexOf("!/");
        if (separatorIndex == -1) {
            throw new CucumberException("Not a jar URL: " + jarUrl.toExternalForm());
        }
        return filePath(new URL(urlFile.substring(0, separatorIndex)));
    }
}
