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

import static java.util.Collections.emptyMap;

class JarUriFileSystemService {

    private static final String FILE_URI_SCHEME = "file";
    private static final String JAR_URI_SCHEME = "jar";
    private static final String JAR_URI_SCHEME_PREFIX = JAR_URI_SCHEME + ":";
    private static final String JAR_FILE_SUFFIX = ".jar";
    private static final String JAR_URI_SEPARATOR = "!";

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
        if (hasJarUriScheme(uri)) {
            return handleJarUriScheme(uri);
        }
        if (hasFileUriSchemeWithJarExtension(uri)) {
            return handleFileUriSchemeWithJarExtension(uri);
        }
        throw new IllegalArgumentException("Unsupported uri " + uri.toString());
    }

    private static CloseablePath handleFileUriSchemeWithJarExtension(URI uri) throws IOException, URISyntaxException {
        return open(new URI(JAR_URI_SCHEME_PREFIX + uri),
            fileSystem -> fileSystem.getRootDirectories().iterator().next());
    }

    private static CloseablePath handleJarUriScheme(URI uri) throws IOException, URISyntaxException {
        String[] parts = uri.toString().split(JAR_URI_SEPARATOR);
        // Regular jar schemes
        if (parts.length <= 2) {
            String jarUri = parts[0];
            String jarPath = parts.length == 2 ? parts[1] : "/";
            return open(new URI(jarUri), fileSystem -> fileSystem.getPath(jarPath));
        }

        // Spring boot jar scheme
        String jarUri = parts[0];
        String jarEntry = parts[1];
        String subEntry = parts[2];
        if (jarEntry.endsWith(JAR_FILE_SUFFIX)) {
            throw nestedJarEntriesAreUnsupported(uri);
        }
        return open(new URI(jarUri), fileSystem -> fileSystem.getPath(jarEntry + subEntry));
    }

    private static CucumberException nestedJarEntriesAreUnsupported(URI uri) {
        return new CucumberException("" +
                "The resource " + uri + " is located in a nested jar.\n" +
                "\n" +
                "This typically happens when trying to run Cucumber inside a Spring Boot Executable Jar.\n" +
                "Cucumber currently doesn't support classpath scanning in nested jars.\n" +
                "Feel free to send a pull request to make this possible!\n" +
                "\n" +
                "You can avoid this error by unpacking your application before executing.");
    }

}
