package io.cucumber.core.model;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.Character.isJavaIdentifierPart;
import static java.lang.Character.isJavaIdentifierStart;

/**
 * The glue path is a class path URI to a package.
 *
 * The glue path can be written as either a package name: {@code com.example.app},
 * a path {@code com/example/app} or uri {@code classpath:com/example/app}.
 *
 * On file system with a path separator other then `{@code /}` {@code com\example\app}
 * is also a valid glue path.
 *
 * It is recommended to always use the package name form.
 *
 */
public class GluePath {
    public static URI parse(String gluePath) {
        if (nonStandardPathSeparatorInUse(gluePath)) {
            String standardized = replaceNonStandardPathSeparator(gluePath);
            return parseAssumeClasspathScheme(standardized);
        }

        if (isProbablyPackage(gluePath)) {
            String path = replacePackageSeparator(gluePath);
            return parseAssumeClasspathScheme(path);
        }

        return parseAssumeClasspathScheme(gluePath);
    }

    private static boolean isProbablyPackage(String gluePath) {
        return gluePath.contains(".") && !gluePath.contains("/");
    }

    private static String replacePackageSeparator(String gluePath) {
        return gluePath.replace('.','/');
    }

    private static String replaceNonStandardPathSeparator(String featureIdentifier) {
        return featureIdentifier.replace(File.separatorChar, '/');
    }

    private static boolean nonStandardPathSeparatorInUse(String featureIdentifier) {
        return File.separatorChar != '/' && featureIdentifier.contains(File.separator);
    }

    private static URI parseAssumeClasspathScheme(String gluePath) {
        URI uri = URI.create(gluePath);

        if(!isValidIdentifier(uri.getSchemeSpecificPart())){
            throw new IllegalArgumentException("The glue path contained invalid identifiers " + uri);
        }

        if(uri.getScheme() == null){
            try {
                return new URI("classpath", uri.getSchemeSpecificPart(), uri.getFragment());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }

        if(!"classpath".equals(uri.getScheme())){
            throw new IllegalArgumentException("The glue path must have a classpath scheme " + uri);
        }

        return uri;
    }

    private static boolean isValidIdentifier(String schemeSpecificPart) {
        for (String part : schemeSpecificPart.split("/")) {
            for (int i = 0; i < part.length(); i++) {
                if (i == 0 && !isJavaIdentifierStart(part.charAt(i)) || (i != 0 && !isJavaIdentifierPart(part.charAt(i)))) {
                    return false;
                }
            }
        }
        return true;
    }
}
