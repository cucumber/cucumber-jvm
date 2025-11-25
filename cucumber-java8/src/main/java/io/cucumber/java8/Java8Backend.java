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
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
        loadGlueClassesImpl(glue, scanForClasses(gluePaths));
    }

    @Override
    public void loadGlueClasses(Glue glue, Set<String> glueClassNames) {
        Set<Class<?>> glueClasses = glueClassNames.stream()
                .map(classFinder::safelyLoadClass)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        loadGlueClassesImpl(glue, glueClasses);
    }

    private void loadGlueClassesImpl(Glue glue, Set<Class<?>> glueClasses) {
        this.glue = new ClosureAwareGlueRegistry(glue);
        glueClasses.stream()
                // Filter Java8 style glue (lambdas)
                .filter(aClass -> !LambdaGlue.class.equals(aClass) && LambdaGlue.class.isAssignableFrom(aClass))
                .map(aClass -> (Class<? extends LambdaGlue>) aClass.asSubclass(LambdaGlue.class))
                .filter(glueClass -> !glueClass.isInterface())
                .filter(glueClass -> glueClass.getConstructors().length > 0)
                .forEach(this::processClass);
    }

    private Set<Class<?>> scanForClasses(List<URI> gluePaths) {
        return gluePaths.stream()
                .filter(gluePath -> ClasspathSupport.CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName)
                .map(classFinder::scanForClassesInPackage)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private void processClass(Class<? extends LambdaGlue> glueClass) {
        container.addClass(glueClass);
        lambdaGlueClasses.add(glueClass);
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
