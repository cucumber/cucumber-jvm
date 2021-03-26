package io.cucumber.core.runtime;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.backend.StaticHookDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.snippets.TestSnippet;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class StubBackendSupplier implements BackendSupplier {

    private final List<StaticHookDefinition> beforeAll;
    private final List<HookDefinition> before;
    private final List<HookDefinition> beforeStep;
    private final List<StepDefinition> steps;
    private final List<HookDefinition> afterStep;
    private final List<HookDefinition> after;
    private final List<StaticHookDefinition> afterAll;

    public StubBackendSupplier(StepDefinition... steps) {
        this(Collections.emptyList(), Arrays.asList(steps), Collections.emptyList());
    }

    public StubBackendSupplier(
            List<HookDefinition> before,
            List<HookDefinition> beforeStep,
            List<StepDefinition> steps,
            List<HookDefinition> afterStep,
            List<HookDefinition> after
    ) {
        this(Collections.emptyList(), before, beforeStep, steps, afterStep, after, Collections.emptyList());
    }

    public StubBackendSupplier(
            List<StaticHookDefinition> beforeAll,
            List<HookDefinition> before,
            List<HookDefinition> beforeStep,
            List<StepDefinition> steps,
            List<HookDefinition> afterStep,
            List<HookDefinition> after,
            List<StaticHookDefinition> afterAll
    ) {
        this.beforeAll = beforeAll;
        this.before = before;
        this.beforeStep = beforeStep;
        this.steps = steps;
        this.afterStep = afterStep;
        this.after = after;
        this.afterAll = afterAll;
    }

    public StubBackendSupplier(
            List<HookDefinition> before,
            List<StepDefinition> steps,
            List<HookDefinition> after
    ) {
        this(before, Collections.emptyList(), steps, Collections.emptyList(), after);
    }

    @Override
    public Collection<? extends Backend> get() {
        return Collections.singletonList(new Backend() {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                beforeAll.forEach(glue::addBeforeAllHook);
                before.forEach(glue::addBeforeHook);
                beforeStep.forEach(glue::addBeforeStepHook);
                steps.forEach(glue::addStepDefinition);
                afterStep.forEach(glue::addAfterStepHook);
                after.forEach(glue::addAfterHook);
                afterAll.forEach(glue::addAfterAllHook);
            }

            @Override
            public void buildWorld() {

            }

            @Override
            public void disposeWorld() {

            }

            @Override
            public Snippet getSnippet() {
                return new TestSnippet();
            }
        });
    }

}
