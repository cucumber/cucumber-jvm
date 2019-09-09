package io.cucumber.java8;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.StepDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicBoolean;

import static io.cucumber.java8.LambdaGlue.DEFAULT_AFTER_ORDER;
import static io.cucumber.java8.LambdaGlue.DEFAULT_BEFORE_ORDER;
import static io.cucumber.java8.LambdaGlue.EMPTY_TAG_EXPRESSION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LambdaGlueTest {

    private HookDefinition beforeStepHook;
    private HookDefinition afterHook;
    private HookDefinition beforeHook;
    private HookDefinition afterStepHook;
    private AtomicBoolean invoked = new AtomicBoolean();

    @BeforeEach
    void setup() {
        LambdaGlueRegistry.INSTANCE.set(lambdaGlueRegistry);
    }

    @Test
    void testBeforeHook() throws Throwable {
        lambdaGlue.Before(this::hookNoArgs);
        assertHook(beforeHook, EMPTY_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER);
        lambdaGlue.Before("taxExpression", this::hookNoArgs);
        assertHook(beforeHook, "taxExpression", DEFAULT_BEFORE_ORDER);
        lambdaGlue.Before(42, this::hookNoArgs);
        assertHook(beforeHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.Before(100L, this::hookNoArgs);
        assertHook(beforeHook, EMPTY_TAG_EXPRESSION, DEFAULT_AFTER_ORDER);
        lambdaGlue.Before("taxExpression", 42, this::hookNoArgs);
        assertHook(beforeHook, "taxExpression", 42);
        lambdaGlue.Before("taxExpression", 100L, 42, this::hookNoArgs);
        assertHook(beforeHook, "taxExpression", 42);

        lambdaGlue.Before(this::hook);
        assertHook(beforeHook, EMPTY_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER);
        lambdaGlue.Before("taxExpression", this::hook);
        assertHook(beforeHook, "taxExpression", DEFAULT_BEFORE_ORDER);
        lambdaGlue.Before(42, this::hook);
        assertHook(beforeHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.Before(100L, this::hook);
        assertHook(beforeHook, EMPTY_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER);
        lambdaGlue.Before("taxExpression", 42, this::hook);
        assertHook(beforeHook, "taxExpression", 42);
        lambdaGlue.Before("taxExpression", 100L, 42, this::hook);
        assertHook(beforeHook, "taxExpression", 42);
    }

    @Test
    void testBeforeStepHook() throws Throwable {
        lambdaGlue.BeforeStep(this::hookNoArgs);
        assertHook(beforeStepHook, EMPTY_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER);
        lambdaGlue.BeforeStep("taxExpression", this::hookNoArgs);
        assertHook(beforeStepHook, "taxExpression", DEFAULT_BEFORE_ORDER);
        lambdaGlue.BeforeStep(42, this::hookNoArgs);
        assertHook(beforeStepHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.BeforeStep(100L, this::hookNoArgs);
        assertHook(beforeStepHook, EMPTY_TAG_EXPRESSION, DEFAULT_AFTER_ORDER);
        lambdaGlue.BeforeStep("taxExpression", 42, this::hookNoArgs);
        assertHook(beforeStepHook, "taxExpression", 42);
        lambdaGlue.BeforeStep("taxExpression", 100L, 42, this::hookNoArgs);
        assertHook(beforeStepHook, "taxExpression", 42);

        lambdaGlue.BeforeStep(this::hook);
        assertHook(beforeStepHook, EMPTY_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER);
        lambdaGlue.BeforeStep("taxExpression", this::hook);
        assertHook(beforeStepHook, "taxExpression", DEFAULT_BEFORE_ORDER);
        lambdaGlue.BeforeStep(42, this::hook);
        assertHook(beforeStepHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.BeforeStep(100L, this::hook);
        assertHook(beforeStepHook, EMPTY_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER);
        lambdaGlue.BeforeStep("taxExpression", 42, this::hook);
        assertHook(beforeStepHook, "taxExpression", 42);
        lambdaGlue.BeforeStep("taxExpression", 100L, 42, this::hook);
        assertHook(beforeStepHook, "taxExpression", 42);
    }

    @Test
    void testAfterHook() throws Throwable {
        lambdaGlue.After(this::hookNoArgs);
        assertHook(afterHook, EMPTY_TAG_EXPRESSION, DEFAULT_AFTER_ORDER);
        lambdaGlue.After("taxExpression", this::hookNoArgs);
        assertHook(afterHook, "taxExpression", DEFAULT_AFTER_ORDER);
        lambdaGlue.After(42, this::hookNoArgs);
        assertHook(afterHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.After(100L, this::hookNoArgs);
        assertHook(afterHook, EMPTY_TAG_EXPRESSION, DEFAULT_AFTER_ORDER);
        lambdaGlue.After("taxExpression", 42, this::hookNoArgs);
        assertHook(afterHook, "taxExpression", 42);
        lambdaGlue.After("taxExpression", 100L, 42, this::hookNoArgs);
        assertHook(afterHook, "taxExpression", 42);

        lambdaGlue.After(this::hook);
        assertHook(afterHook, EMPTY_TAG_EXPRESSION, DEFAULT_AFTER_ORDER);
        lambdaGlue.After("taxExpression", this::hook);
        assertHook(afterHook, "taxExpression", DEFAULT_AFTER_ORDER);
        lambdaGlue.After(42, this::hook);
        assertHook(afterHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.After(100L, this::hook);
        assertHook(afterHook, EMPTY_TAG_EXPRESSION, DEFAULT_AFTER_ORDER);
        lambdaGlue.After("taxExpression", 42, this::hook);
        assertHook(afterHook, "taxExpression", 42);
        lambdaGlue.After("taxExpression", 100L, 42, this::hook);
        assertHook(afterHook, "taxExpression", 42);
    }

    @Test
    void testAfterStepHook() throws Throwable {
        lambdaGlue.AfterStep(this::hookNoArgs);
        assertHook(afterStepHook, EMPTY_TAG_EXPRESSION, DEFAULT_AFTER_ORDER);
        lambdaGlue.AfterStep("taxExpression", this::hookNoArgs);
        assertHook(afterStepHook, "taxExpression", DEFAULT_AFTER_ORDER);
        lambdaGlue.AfterStep(42, this::hookNoArgs);
        assertHook(afterStepHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.AfterStep(100L, this::hookNoArgs);
        assertHook(afterStepHook, EMPTY_TAG_EXPRESSION, DEFAULT_AFTER_ORDER);
        lambdaGlue.AfterStep("taxExpression", 42, this::hookNoArgs);
        assertHook(afterStepHook, "taxExpression", 42);
        lambdaGlue.AfterStep("taxExpression", 100L, 42, this::hookNoArgs);
        assertHook(afterStepHook, "taxExpression", 42);

        lambdaGlue.AfterStep(this::hook);
        assertHook(afterStepHook, EMPTY_TAG_EXPRESSION, DEFAULT_AFTER_ORDER);
        lambdaGlue.AfterStep("taxExpression", this::hook);
        assertHook(afterStepHook, "taxExpression", DEFAULT_AFTER_ORDER);
        lambdaGlue.AfterStep(42, this::hook);
        assertHook(afterStepHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.AfterStep(100L, this::hook);
        assertHook(afterStepHook, EMPTY_TAG_EXPRESSION, DEFAULT_AFTER_ORDER);
        lambdaGlue.AfterStep("taxExpression", 42, this::hook);
        assertHook(afterStepHook, "taxExpression", 42);
        lambdaGlue.AfterStep("taxExpression", 100L, 42, this::hook);
        assertHook(afterStepHook, "taxExpression", 42);
    }

    private final io.cucumber.core.backend.Scenario scenario = Mockito.mock(io.cucumber.core.backend.Scenario.class);

    private final LambdaGlueRegistry lambdaGlueRegistry = new LambdaGlueRegistry() {
        @Override
        public void addStepDefinition(StepDefinition stepDefinition) {
        }

        @Override
        public void addBeforeStepHookDefinition(HookDefinition beforeStepHook) {
            LambdaGlueTest.this.beforeStepHook = beforeStepHook;

        }

        @Override
        public void addAfterStepHookDefinition(HookDefinition afterStepHook) {
            LambdaGlueTest.this.afterStepHook = afterStepHook;

        }

        @Override
        public void addBeforeHookDefinition(HookDefinition beforeHook) {
            LambdaGlueTest.this.beforeHook = beforeHook;

        }

        @Override
        public void addAfterHookDefinition(HookDefinition afterHook) {
            LambdaGlueTest.this.afterHook = afterHook;
        }
    };

    private final LambdaGlue lambdaGlue = new LambdaGlue() {

    };

    private void assertHook(HookDefinition hook, String tagExpression, int beforeOrder) throws Throwable {
        assertThat(hook.getTagExpression(), is(tagExpression));
        assertThat(hook.getOrder(), is(beforeOrder));
        hook.execute(scenario);
        assertTrue(invoked.get());
        invoked.set(false);
    }

    void hookNoArgs() {
        invoked.set(true);
    }

    void hook(Scenario scenario) {
        invoked.set(true);
    }

}