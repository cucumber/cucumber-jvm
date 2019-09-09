package io.cucumber.core.io;

import java.net.URI;

public class Classpath {

    public static final String CLASSPATH_SCHEME = "classpath";
    public static final String CLASSPATH_SCHEME_PREFIX = CLASSPATH_SCHEME + ":";

    private Classpath() {

    }

    /**
     * Returns a resource name as defined by {@link ClassLoader#getResource(String)}
     *
     * @param uri to resource
     * @return resource name
     */
    static String resourceName(URI uri) {
        if (!CLASSPATH_SCHEME.equals(uri.getScheme())) {
            throw new IllegalArgumentException("uri must have classpath scheme " + uri);
        }

        String resourceName = uri.getSchemeSpecificPart();
        if (resourceName.startsWith("/")) {
            return resourceName.substring(1);
        }
        return resourceName;
    }
}
