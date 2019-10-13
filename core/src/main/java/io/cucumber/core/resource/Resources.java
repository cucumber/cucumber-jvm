package io.cucumber.core.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

import static io.cucumber.core.resource.Classpath.CLASSPATH_SCHEME_PREFIX;

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
        return (baseDir, resource) -> new ClasspathResource(classpathResourceName, resource);
    }

    private static class ClasspathResource implements Resource {

        private final URI uri;
        private final Path resource;

        ClasspathResource(Path baseDir, Path resource) {
            this.uri = URI.create(CLASSPATH_SCHEME_PREFIX + baseDir.relativize(resource).toString());
            this.resource = resource;
        }

        ClasspathResource(String classpathResourceName, Path resource) {
            this.uri = URI.create(CLASSPATH_SCHEME_PREFIX + classpathResourceName);
            this.resource = resource;
        }

        @Override
        public URI getPath() {
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
        public URI getPath() {
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
            this.uri = URI.create(CLASSPATH_SCHEME_PREFIX + ClasspathSupport.determineFullyQualifiedResourceName(baseDir, packagePath, resource));
            this.resource = resource;
        }

        @Override
        public URI getPath() {
            return uri;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(resource);
        }
    }

}
