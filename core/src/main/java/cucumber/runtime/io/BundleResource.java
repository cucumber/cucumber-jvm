package cucumber.runtime.io;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Example1: bundleresource://587.fwk715858581/cucumber/io/BundleResource.class
 * Example2: bundleresource://587.fwk715858581/cucumber/io/BundleResource$1.class
 * Example3: bundle://587.fwk715858581/cucumber/io/BundleResource.class
 * Example4: bundle://587.fwk715858581/cucumber/io/BundleResource$1.class
 *
 * @author mdelapenya
 */
public class BundleResource implements Resource {

    private final URL bundleURL;
    private final URL entryURL;

    public BundleResource(URL bundleURL, URL entryURL) {
        this.bundleURL = bundleURL;
        this.entryURL = entryURL;
    }

    @Override
    public String getPath() {
        return entryURL.getPath();
    }

    @Override
    public String getAbsolutePath() {
        return bundleURL.getPath() + getPath();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return bundleURL.openStream();
    }

    @Override
    public String getClassName(String extension) {
        if (entryURL == null) {
            throw new IllegalArgumentException(
                "The resource does not exist in the bundle");
        }

        String entryURLPath = entryURL.toString();

        Pattern pattern = Pattern.compile("bundle(entry)?://\\d+\\.\\w+(?::\\d+)?/(\\S+)");

        Matcher matcher = pattern.matcher(entryURLPath);

        if (matcher.matches()) {
            String fullyQualifiedClassName = matcher.group(2);

            return fullyQualifiedClassName.replace("/", ".");
        }

        throw new IllegalArgumentException(
            "The resource is not a valid bundle resource");
    }

}
