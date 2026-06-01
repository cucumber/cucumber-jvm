package io.cucumber.guice;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.resource.ClasspathSupport;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;

final class GuiceBackend implements Backend {

    private final Container container;
    private final ClasspathScanner classFinder;

    GuiceBackend(Container container, Supplier<ClassLoader> classLoaderSupplier) {
        this.container = container;
        this.classFinder = new ClasspathScanner(classLoaderSupplier);
    }

    @Override
    public void loadGlue(Glue glue, GlueDiscoveryRequest discoveryRequest) {
        Stream<Class<?>> glueClasses = discoveryRequest.getGlue()
                .stream()
                .filter(gluePath -> CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName)
                .map(classFinder::scanForClassesInPackage)
                .flatMap(Collection::stream);

        Stream<Class<?>> explicitClasses = discoveryRequest.getGlueClassNames()
                .stream()
                .map(classFinder::loadClass);

        Stream.concat(glueClasses, explicitClasses)
                .distinct()
                .forEach(container::addClass);
    }

}
