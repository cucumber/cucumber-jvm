package io.cucumber.java8;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.resource.ClasspathSupport;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static io.cucumber.java8.LambdaGlueRegistry.CLOSED;

final class Java8Backend implements Backend {

    private final Lookup lookup;
    private final Container container;
    private final ClasspathScanner classFinder;

    private final List<Class<? extends LambdaGlue>> lambdaGlueClasses = new ArrayList<>();
    private ClosureAwareGlueRegistry glue;

    Java8Backend(Lookup lookup, Container container, Supplier<ClassLoader> classLoaderProvider) {
        this.container = container;
        this.lookup = lookup;
        this.classFinder = new ClasspathScanner(classLoaderProvider);
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        this.glue = new ClosureAwareGlueRegistry(glue);
        // Scan for Java8 style glue (lambdas)
        gluePaths.stream()
                .filter(gluePath -> ClasspathSupport.CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName)
                .map(basePackageName -> classFinder.scanForSubClassesInPackage(basePackageName, LambdaGlue.class))
                .flatMap(Collection::stream)
                .filter(glueClass -> !glueClass.isInterface())
                .filter(glueClass -> glueClass.getConstructors().length > 0)
                .distinct()
                .forEach(glueClass -> {
                    container.addClass(glueClass);
                    lambdaGlueClasses.add(glueClass);
                });
    }

    @Override
    public void buildWorld() {
        // Instantiate all the stepdef classes for java8 - the stepdef will be
        // initialised in the constructor.
        glue.startRegistration();
        LambdaGlueRegistry.INSTANCE.set(glue);
        for (Class<? extends LambdaGlue> lambdaGlueClass : lambdaGlueClasses) {
            lookup.getInstance(lambdaGlueClass);
        }
        LambdaGlueRegistry.INSTANCE.set(CLOSED);
        glue.finishRegistration();
    }

    @Override
    public void disposeWorld() {
        glue.disposeClosures();
    }

    @Override
    public Snippet getSnippet() {
        return new Java8Snippet();
    }

}
