package io.cucumber.core.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME_PREFIX;
import static io.cucumber.core.resource.ClasspathSupport.determineFullyQualifiedResourceName;

class Resources {

    private Resources() {

    }

    static BiFunction<Path, Path, Resource> createPackageResource(String packageName) {
        return (baseDir, resource) -> new PackageResource(baseDir, packageName, resource);
    }

    static BiFunction<Path, Path, Resource> createUriResource() {
        return (baseDir, resource) -> new UriResource(resource);
    }

    static BiFunction<Path, Path, Resource> createClasspathRootResource() {
        return ClasspathResource::new;
    }

    static BiFunction<Path, Path, Resource> createClasspathResource(String classpathResourceName) {
        return (baseDir, resource) -> new ClasspathResource(classpathResourceName, baseDir, resource);
    }

    private static class ClasspathResource implements Resource {

        private final URI uri;
        private final Path resource;

        ClasspathResource(Path baseDir, Path resource) {
            this.uri = URI.create(CLASSPATH_SCHEME_PREFIX + baseDir.relativize(resource).toString());
            this.resource = resource;
        }

        ClasspathResource(String classpathResourceName, Path baseDir, Path resource) {
            if (baseDir.equals(resource)) {
                this.uri = URI.create(CLASSPATH_SCHEME_PREFIX + classpathResourceName);
            } else {
                // classpathResourceName was a package
                String fqn = determineFullyQualifiedResourceName(baseDir, classpathResourceName, resource);
                this.uri = URI.create(CLASSPATH_SCHEME_PREFIX + fqn);
            }
            this.resource = resource;
        }

        @Override
        public URI getUri() {
            return uri;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(resource);
        }
    }

    private static class UriResource implements Resource {
        private final Path resource;

        UriResource(Path resource) {
            this.resource = resource;
        }

        @Override
        public URI getUri() {
            return resource.toUri();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(resource);
        }
    }

    private static class PackageResource implements Resource {
        private final Path resource;
        private final URI uri;

        PackageResource(Path baseDir, String packageName, Path resource) {
            String packagePath = ClasspathSupport.packagePath(packageName);
            String fqn = determineFullyQualifiedResourceName(baseDir, packagePath, resource);
            this.uri = URI.create(CLASSPATH_SCHEME_PREFIX + fqn);
            this.resource = resource;
        }

        @Override
        public URI getUri() {
            return uri;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(resource);
        }
    }

}
