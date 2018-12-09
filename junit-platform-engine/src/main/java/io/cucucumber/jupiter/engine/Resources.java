package io.cucucumber.jupiter.engine;

import cucumber.runtime.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.cucucumber.jupiter.engine.ClasspathSupport.determineFullyQualifiedResourceName;

class Resources {
    private static final String CLASSPATH_SCHEME = "classpath:";

    private Resources() {

    }

    static Resource createPackageResource(Path baseDir, String packageName, Path resource) {
        return new PackageResource(baseDir, packageName, resource);
    }

    static Resource createUriResource(Path resource) {
        return new UriResource(resource);
    }

    static Resource createClasspathRootResource(Path baseDir, Path resource) {
        return new ClasspathResource(baseDir, resource);
    }

    static Resource createClasspathResource(String classpathResourceName, Path resource) {
        return new ClasspathResource(classpathResourceName, resource);
    }

    private static class ClasspathResource implements Resource {

        private final String uri;
        private final Path resource;

        ClasspathResource(Path baseDir, Path resource) {
            this.uri = CLASSPATH_SCHEME + baseDir.relativize(resource).toString();
            this.resource = resource;
        }

        ClasspathResource(String classpathResourceName, Path resource) {
            this.uri = CLASSPATH_SCHEME + classpathResourceName;
            this.resource = resource;
        }

        @Override
        public String getPath() {
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
        public String getPath() {
            return resource.toUri().toString();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(resource);
        }
    }

    private static class PackageResource implements Resource {
        private final Path resource;
        private final String uri;

        PackageResource(Path baseDir, String packageName, Path resource) {
            String packagePath = ClasspathSupport.packagePath(packageName);
            this.uri = CLASSPATH_SCHEME + determineFullyQualifiedResourceName(baseDir, packagePath, resource);
            this.resource = resource;
        }

        @Override
        public String getPath() {
            return uri;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(resource);
        }
    }

}
