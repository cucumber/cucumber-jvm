package io.cucumber.java8;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.snippets.Snippet;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Thread.currentThread;

final class Java8Backend implements Backend {

    private final Lookup lookup;
    private final Container container;
    private final ClassFinder classFinder;

    private final List<Class<? extends LambdaGlue>> lambdaGlueClasses = new ArrayList<>();
    private Glue glue;

    Java8Backend(Lookup lookup, Container container, ResourceLoader resourceLoader) {
        this.classFinder = new ResourceLoaderClassFinder(resourceLoader, currentThread().getContextClassLoader());
        this.container = container;
        this.lookup = lookup;
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        this.glue = glue;
        // Scan for Java8 style glue (lambdas)
        gluePaths.stream()
            .map(packageName -> classFinder.getDescendants(LambdaGlue.class, packageName))
            .flatMap(Collection::stream)
            .filter(glueClass -> !glueClass.isInterface())
            .filter(glueClass -> glueClass.getConstructors().length > 0)
            .forEach(glueClass -> {
                if (container.addClass(glueClass)) {
                    lambdaGlueClasses.add(glueClass);
                }
            });
    }

    @Override
    public void buildWorld() {
        // Instantiate all the stepdef classes for java8 - the stepdef will be initialised
        // in the constructor.
        LambdaGlueRegistry.INSTANCE.set(new GlueAdaptor(glue));
        for (Class<? extends LambdaGlue> lambdaGlueClass : lambdaGlueClasses) {
            lookup.getInstance(lambdaGlueClass);
        }
    }

    @Override
    public void disposeWorld() {
        LambdaGlueRegistry.INSTANCE.remove();
    }

    @Override
    public Snippet getSnippet() {
        return new Java8Snippet();
    }


    private static final class GlueAdaptor implements LambdaGlueRegistry {

        private final Glue glue;

        private GlueAdaptor(Glue glue) {
            this.glue = glue;
        }

        @Override
        public void addStepDefinition(StepDefinition stepDefinition) {
            glue.addStepDefinition(stepDefinition);
        }

        @Override
        public void addBeforeStepHookDefinition(HookDefinition beforeStepHook) {
            glue.addBeforeStepHook(beforeStepHook);

        }

        @Override
        public void addAfterStepHookDefinition(HookDefinition afterStepHook) {
            glue.addAfterStepHook(afterStepHook);

        }

        @Override
        public void addBeforeHookDefinition(HookDefinition beforeHook) {
            glue.addBeforeHook(beforeHook);

        }

        @Override
        public void addAfterHookDefinition(HookDefinition afterHook) {
            glue.addAfterHook(afterHook);

        }
    }
}
