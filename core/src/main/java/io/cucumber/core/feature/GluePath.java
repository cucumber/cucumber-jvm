package io.cucumber.core.feature;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;
import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME_PREFIX;
import static io.cucumber.core.resource.ClasspathSupport.PACKAGE_SEPARATOR_STRING;
import static io.cucumber.core.resource.ClasspathSupport.RESOURCE_SEPARATOR_CHAR;
import static io.cucumber.core.resource.ClasspathSupport.RESOURCE_SEPARATOR_STRING;
import static io.cucumber.core.resource.ClasspathSupport.resourceNameOfPackageName;
import static io.cucumber.core.resource.ClasspathSupport.rootPackageUri;
import static java.lang.Character.isJavaIdentifierPart;
import static java.lang.Character.isJavaIdentifierStart;
import static java.util.Objects.requireNonNull;

/**
 * The glue path is a class path URI to a package.
 * <p>
 * The glue path can be written as either a package name:
 * {@code com.example.app}, a path {@code com/example/app} or uri
 * {@code classpath:com/example/app}.
 * <p>
 * On file system with a path separator other then `{@code /}`
 * {@code com\example\app} is also a valid glue path.
 * <p>
 * It is recommended to always use the package name form.
 */
public class GluePath {

    private GluePath() {

    }

    public static URI parse(String gluePath) {
        requireNonNull(gluePath, "gluePath may not be null");
        if (gluePath.isEmpty()) {
            return rootPackageUri();
        }

        // Legacy from the Cucumber Eclipse plugin
        // Older versions of Cucumber allowed it.
        if (CLASSPATH_SCHEME_PREFIX.equals(gluePath)) {
            return rootPackageUri();
        }

        if (nonStandardPathSeparatorInUse(gluePath)) {
            String standardized = replaceNonStandardPathSeparator(gluePath);
            return parseAssumeClasspathScheme(standardized);
        }

        if (isProbablyPackage(gluePath)) {
            String path = resourceNameOfPackageName(gluePath);
            return parseAssumeClasspathScheme(path);
        }

        return parseAssumeClasspathScheme(gluePath);
    }

    private static boolean nonStandardPathSeparatorInUse(String featureIdentifier) {
        return File.separatorChar != RESOURCE_SEPARATOR_CHAR
                && featureIdentifier.contains(File.separator);
    }

    private static String replaceNonStandardPathSeparator(String featureIdentifier) {
        return featureIdentifier.replace(File.separatorChar, RESOURCE_SEPARATOR_CHAR);
    }

    private static URI parseAssumeClasspathScheme(String gluePath) {
        URI uri = URI.create(gluePath);

        String schemeSpecificPart = uri.getSchemeSpecificPart();
        if (!isValidIdentifier(schemeSpecificPart)) {
            throw new IllegalArgumentException("The glue path contained invalid identifiers " + uri);
        }

        if (uri.getScheme() == null) {
            try {
                return new URI(CLASSPATH_SCHEME,
                    schemeSpecificPart.startsWith("/") ? schemeSpecificPart : "/" + schemeSpecificPart,
                    uri.getFragment());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }

        if (!CLASSPATH_SCHEME.equals(uri.getScheme())) {
            throw new IllegalArgumentException("The glue path must have a classpath scheme " + uri);
        }

        return uri;
    }

    private static boolean isProbablyPackage(String gluePath) {
        return gluePath.contains(PACKAGE_SEPARATOR_STRING)
                && !gluePath.contains(RESOURCE_SEPARATOR_STRING);
    }

    private static boolean isValidIdentifier(String schemeSpecificPart) {
        for (String part : schemeSpecificPart.split("/")) {
            for (int i = 0; i < part.length(); i++) {
                if (i == 0 && !isJavaIdentifierStart(part.charAt(i))
                        || (i != 0 && !isJavaIdentifierPart(part.charAt(i)))) {
                    return false;
                }
            }
        }
        return true;
    }

}
