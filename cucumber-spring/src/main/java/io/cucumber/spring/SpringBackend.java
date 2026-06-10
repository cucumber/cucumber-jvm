package io.cucumber.spring;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.GlueDiscoverySelector;
import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.resource.ClasspathSupport;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.function.Supplier;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;

final class SpringBackend implements Backend {

    private final Container container;
    private final ClasspathScanner classFinder;

    SpringBackend(Container container, Supplier<ClassLoader> classLoaderSupplier) {
        this.container = container;
        this.classFinder = new ClasspathScanner(classLoaderSupplier);
    }

    @Override
    public void loadGlue(Glue glue, GlueDiscoveryRequest request) {
        request.getSelectorsByType(GlueDiscoverySelector.UriGlueDiscoverySelector.class) //
                .stream() //
                .map(GlueDiscoverySelector.UriGlueDiscoverySelector::uri)
                .filter(gluePath -> CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName)
                .map(classFinder::scanForClassesInPackage)
                .flatMap(Collection::stream)
                .filter(SpringFactory::hasCucumberContextConfiguration)
                .filter(this::checkIfOfClassTypeAndNotAbstract)
                .distinct()
                .forEach(container::addClass);
    }

    private boolean checkIfOfClassTypeAndNotAbstract(Class<?> clazz) {
        return !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
    }
}
