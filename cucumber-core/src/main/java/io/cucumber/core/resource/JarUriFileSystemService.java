package io.cucumber.core.resource;

import io.cucumber.core.exception.CucumberException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static io.cucumber.core.resource.ClasspathSupport.nestedJarEntriesExplanation;
import static java.util.Collections.emptyMap;

class JarUriFileSystemService {

    private static final String FILE_URI_SCHEME = "file";
    private static final String JAR_URI_SCHEME = "jar";
    private static final String JAR_URI_SCHEME_PREFIX = JAR_URI_SCHEME + ":";
    private static final String JAR_FILE_SUFFIX = ".jar";
    private static final String JAR_URI_SEPARATOR = "!/";

    private static final Map<URI, FileSystem> openFiles = new HashMap<>();
    private static final Map<URI, AtomicInteger> referenceCount = new HashMap<>();

    private static CloseablePath open(URI jarUri, Function<FileSystem, Path> pathProvider)
            throws IOException {
        FileSystem fileSystem = openFileSystem(jarUri);
        Path path = pathProvider.apply(fileSystem);
        return CloseablePath.open(path, () -> closeFileSystem(jarUri));
    }

    private synchronized static void closeFileSystem(URI jarUri) throws IOException {
        int referents = referenceCount.get(jarUri).decrementAndGet();
        if (referents == 0) {
            openFiles.remove(jarUri).close();
            referenceCount.remove(jarUri);
        }
    }

    private synchronized static FileSystem openFileSystem(URI jarUri) throws IOException {
        FileSystem existing = openFiles.get(jarUri);
        if (existing != null) {
            referenceCount.get(jarUri).getAndIncrement();
            return existing;
        }
        FileSystem fileSystem = FileSystems.newFileSystem(jarUri, emptyMap());
        openFiles.put(jarUri, fileSystem);
        referenceCount.put(jarUri, new AtomicInteger(1));
        return fileSystem;
    }

    static boolean supports(URI uri) {
        return hasJarUriScheme(uri) || hasFileUriSchemeWithJarExtension(uri);
    }

    private static boolean hasJarUriScheme(URI uri) {
        return JAR_URI_SCHEME.equals(uri.getScheme());
    }

    private static boolean hasFileUriSchemeWithJarExtension(URI uri) {
        return FILE_URI_SCHEME.equals(uri.getScheme()) && uri.getPath().endsWith(JAR_FILE_SUFFIX);
    }

    static CloseablePath open(URI uri) throws URISyntaxException, IOException {
        assert supports(uri);
        if (hasFileUriSchemeWithJarExtension(uri)) {
            return handleFileUriSchemeWithJarExtension(uri);
        }
        if (isSpringBoot31OrLower(uri)) {
            return handleSpringBoot31JarUri(uri);
        }
        return handleJarUriScheme(uri);
    }

    private static CloseablePath handleFileUriSchemeWithJarExtension(URI uri) throws IOException, URISyntaxException {
        return open(new URI(JAR_URI_SCHEME_PREFIX + uri),
            fileSystem -> fileSystem.getRootDirectories().iterator().next());
    }

    private static CloseablePath handleJarUriScheme(URI uri) throws IOException, URISyntaxException {
        // Regular Jar Uris
        // Format: jar:<url>!/[<entry>]
        String uriString = uri.toString();
        int lastJarUriSeparator = uriString.lastIndexOf(JAR_URI_SEPARATOR);
        if (lastJarUriSeparator < 0) {
            throw new IllegalArgumentException(String.format("jar uri '%s' must contain '%s'", uri, JAR_URI_SEPARATOR));
        }
        String url = uriString.substring(0, lastJarUriSeparator);
        String entry = uriString.substring(lastJarUriSeparator + 1);
        return open(new URI(url), fileSystem -> fileSystem.getPath(entry));
    }

    private static boolean isSpringBoot31OrLower(URI uri) {
        // Starting Spring Boot 3.2 the nested scheme is used. This works with
        // regular jar file handling and doesn't need a workaround.
        // Example 3.2:
        // jar:nested:/dir/myjar.jar/!/BOOT-INF/lib/nested.jar!/com/example/MyClass.class
        // Example 3.1:
        // jar:file:/dir/myjar.jar/!/BOOT-INF/lib/nested.jar!/com/example/MyClass.class
        String schemeSpecificPart = uri.getSchemeSpecificPart();
        return schemeSpecificPart.startsWith("file:") && schemeSpecificPart.contains("!/BOOT-INF");
    }

    private static CloseablePath handleSpringBoot31JarUri(URI uri) throws IOException, URISyntaxException {
        // Spring boot 3.1 jar scheme
        // Examples:
        // jar:file:/home/user/application.jar!/BOOT-INF/lib/dependency.jar!/com/example/dependency/resource.txt
        // jar:file:/home/user/application.jar!/BOOT-INF/classes!/com/example/package/resource.txt
        String[] parts = uri.toString().split("!");
        String jarUri = parts[0];
        String jarEntry = parts[1];
        String subEntry = parts[2];
        if (jarEntry.endsWith(JAR_FILE_SUFFIX)) {
            throw new CucumberException(nestedJarEntriesExplanation(uri));
        }
        // We're looking directly at the files in the jar, so we construct the
        // file path by concatenating the jarEntry and subEntry without the jar
        // uri separator.
        return open(new URI(jarUri), fileSystem -> fileSystem.getPath(jarEntry + subEntry));
    }
}
