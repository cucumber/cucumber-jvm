package io.cucumber.core.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

import static io.cucumber.core.resource.ClasspathSupport.classpathResourceUri;
import static io.cucumber.core.resource.ClasspathSupport.determineClasspathResourceUri;
import static io.cucumber.core.resource.ClasspathSupport.resourceNameOfPackageName;

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
            this.uri = classpathResourceUri(baseDir.relativize(resource));
            this.resource = resource;
        }

        ClasspathResource(String classpathResourceName, Path baseDir, Path resource) {
            if (baseDir.equals(resource)) {
                this.uri = classpathResourceUri(classpathResourceName);
            } else {
                // classpathResourceName was a package
                this.uri = determineClasspathResourceUri(baseDir, classpathResourceName, resource);
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
            String classpathResourceName = resourceNameOfPackageName(packageName);
            this.uri = determineClasspathResourceUri(baseDir, classpathResourceName, resource);
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
