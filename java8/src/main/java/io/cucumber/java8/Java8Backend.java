package io.cucumber.java8;

import gherkin.pickles.PickleStep;
import io.cucumber.core.api.options.SnippetType;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.snippets.SnippetGenerator;
import io.cucumber.core.stepexpression.TypeRegistry;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.lang.Thread.currentThread;

public class Java8Backend implements Backend, LambdaGlueRegistry {

    private final TypeRegistry typeRegistry;
    private final SnippetGenerator lambdaSnippetGenerator;

    private final Container container;
    private final ClassFinder classFinder;

    private Glue glue;
    private List<Class<? extends LambdaGlue>> lambdaGlueClasses = new ArrayList<>();

    Java8Backend(Container container, ResourceLoader resourceLoader, TypeRegistry typeRegistry) {
        this.classFinder = new ResourceLoaderClassFinder(resourceLoader, currentThread().getContextClassLoader());
        this.container = container;
        this.lambdaSnippetGenerator = new SnippetGenerator(new Java8Snippet(), typeRegistry.parameterTypeRegistry());
        this.typeRegistry = typeRegistry;
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        this.glue = glue;
        // Scan for Java8 style glue (lambdas)
        for (final URI packageName : gluePaths) {
            Collection<Class<? extends LambdaGlue>> glueDefinerClasses = classFinder.getDescendants(LambdaGlue.class, packageName);
            for (final Class<? extends LambdaGlue> glueClass : glueDefinerClasses) {
                if (glueClass.isInterface()) {
                    continue;
                }

                if (container.addClass(glueClass)) {
                    lambdaGlueClasses.add(glueClass);
                }
            }
        }
    }

    @Override
    public void buildWorld() {
        // Instantiate all the stepdef classes for java8 - the stepdef will be initialised
        // in the constructor.
        try {
            INSTANCE.set(this);
            for (Class<? extends LambdaGlue> lambdaGlueClass: lambdaGlueClasses) {
                container.getInstance(lambdaGlueClass);
            }
        } finally {
            INSTANCE.remove();
        }
    }

    @Override
    public void disposeWorld() {

    }

    @Override
    public List<String> getSnippet(PickleStep step, String keyword, SnippetType.FunctionNameGenerator functionNameGenerator) {
        return lambdaSnippetGenerator.getSnippet(step, keyword, functionNameGenerator);
    }

    @Override
    public void addStepDefinition(Function<TypeRegistry, StepDefinition> stepDefinitionFunction) {
        glue.addStepDefinition(stepDefinitionFunction.apply(typeRegistry));
    }

    @Override
    public void addBeforeHookDefinition(HookDefinition beforeHook) {
        glue.addBeforeHook(beforeHook);
    }

    @Override
    public void addAfterHookDefinition(HookDefinition afterHook) {
        glue.addAfterHook(afterHook);
    }

    @Override
    public void addAfterStepHookDefinition(HookDefinition afterStepHook) {
        glue.addAfterStepHook(afterStepHook);
    }

    @Override
    public void addBeforeStepHookDefinition(HookDefinition beforeStepHook) {
        glue.addBeforeStepHook(beforeStepHook);

    }
}
