package io.cucumber.java8;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.GlueDiscoverySelectorResolver;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.resource.ClasspathScanner;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static io.cucumber.java8.LambdaGlueRegistry.CLOSED;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.requireNonNull;

final class Java8Backend implements Backend {

    private final Lookup lookup;
    private final Container container;
    private final GlueDiscoverySelectorResolver resolver;

    private final List<Class<?>> lambdaGlueClasses = new ArrayList<>();
    private @Nullable ClosureAwareGlueRegistry glue;

    Java8Backend(Lookup lookup, Container container, Supplier<ClassLoader> classLoaderSupplier) {
        this.container = container;
        this.lookup = lookup;
        this.resolver = new GlueDiscoverySelectorResolver( //
            new ClasspathScanner(classLoaderSupplier), //
            Java8Backend::isInstantiableLambdaGlue //
        );
    }

    @Override
    public void loadGlue(Glue glue, GlueDiscoveryRequest request) {
        this.glue = new ClosureAwareGlueRegistry(glue);
        resolver.resolve(request).forEach(glueClass -> {
            container.addClass(glueClass);
            lambdaGlueClasses.add(glueClass);
        });
    }

    @Override
    public void buildWorld() {
        // Instantiate all the stepdef classes for java8 - the stepdef will be
        // initialised in the constructor.
        requireNonNull(glue).startRegistration();
        LambdaGlueRegistry.INSTANCE.set(glue);
        for (Class<?> lambdaGlueClass : lambdaGlueClasses) {
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

    private static boolean isInstantiableLambdaGlue(Class<?> aClass) {
        return LambdaGlue.class.isAssignableFrom(aClass) && isInstantiable(aClass);
    }

    private static boolean isInstantiable(Class<?> clazz) {
        return !clazz.isInterface()
                && !isPrivate(clazz.getModifiers())
                && !isAbstract(clazz.getModifiers())
                && (isStatic(clazz.getModifiers()) || clazz.getEnclosingClass() == null);
    }
}
