package io.cucumber.java8;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.resource.ClasspathSupport;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;
import static io.cucumber.java8.LambdaGlueRegistry.CLOSED;
import static java.util.Objects.requireNonNull;

final class Java8Backend implements Backend {

    private final Lookup lookup;
    private final Container container;
    private final ClasspathScanner classFinder;

    private final List<Class<? extends LambdaGlue>> lambdaGlueClasses = new ArrayList<>();
    private @Nullable ClosureAwareGlueRegistry glue;

    Java8Backend(Lookup lookup, Container container, Supplier<ClassLoader> classLoaderProvider) {
        this.container = container;
        this.lookup = lookup;
        this.classFinder = new ClasspathScanner(classLoaderProvider);
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

        this.glue = new ClosureAwareGlueRegistry(glue);

        Stream.concat(glueClasses, explicitClasses)
                .filter(aClass -> !LambdaGlue.class.equals(aClass) && LambdaGlue.class.isAssignableFrom(aClass))
                .map(aClass -> (Class<? extends LambdaGlue>) aClass.asSubclass(LambdaGlue.class))
                .filter(glueClass -> !glueClass.isInterface())
                .filter(glueClass -> glueClass.getConstructors().length > 0)
                .distinct()
                .forEach(this::processClass);
    }

    private void processClass(Class<? extends LambdaGlue> glueClass) {
        container.addClass(glueClass);
        lambdaGlueClasses.add(glueClass);
    }

    @Override
    public void buildWorld() {
        // Instantiate all the stepdef classes for java8 - the stepdef will be
        // initialised in the constructor.
        requireNonNull(glue).startRegistration();
        LambdaGlueRegistry.INSTANCE.set(glue);
        for (Class<? extends LambdaGlue> lambdaGlueClass : lambdaGlueClasses) {
            lookup.getInstance(lambdaGlueClass);
        }
        LambdaGlueRegistry.INSTANCE.set(CLOSED);
        glue.finishRegistration();
    }

    @Override
    public void disposeWorld() {
        requireNonNull(glue).disposeClosures();
    }

    @Override
    public Snippet getSnippet() {
        return new Java8Snippet();
    }

}
