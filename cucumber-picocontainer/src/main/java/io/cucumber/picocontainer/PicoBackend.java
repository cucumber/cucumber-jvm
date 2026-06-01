package io.cucumber.picocontainer;

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

final class PicoBackend implements Backend {

    private final Container container;
    private final ClasspathScanner classFinder;

    PicoBackend(Container container, Supplier<ClassLoader> classLoaderSupplier) {
        this.container = container;
        this.classFinder = new ClasspathScanner(classLoaderSupplier);
    }

    @Override
    public void loadGlue(Glue glue, GlueDiscoveryRequest glueDiscoveryRequest) {
        Stream<Class<?>> glueClasses = glueDiscoveryRequest.getGlue()
                .stream()
                .filter(gluePath -> CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName)
                .map(classFinder::scanForClassesInPackage)
                .flatMap(Collection::stream);

        Stream<Class<?>> explicitClasses = glueDiscoveryRequest.getGlueClassNames()
                .stream()
                .map(classFinder::loadClass);

        Stream.concat(glueClasses, explicitClasses)
                .filter(PicoBackend::hasCucumberPicoProvider)
                .distinct()
                .forEach(container::addClass);
    }

    private static boolean hasCucumberPicoProvider(Class<?> clazz) {
        return clazz.isAnnotationPresent(CucumberPicoProvider.class);
    }

}
