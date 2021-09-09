package io.cucumber.java8;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.TestCaseState;
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

    private final AtomicBoolean invoked = new AtomicBoolean();
    private final TestCaseState state = Mockito.mock(TestCaseState.class);
    private final LambdaGlue lambdaGlue = new LambdaGlue() {

    };
    private HookDefinition beforeStepHook;
    private HookDefinition afterHook;
    private HookDefinition beforeHook;
    private HookDefinition afterStepHook;
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

        @Override
        public void addDocStringType(DocStringTypeDefinition docStringType) {
        }

        @Override
        public void addDataTableType(DataTableTypeDefinition dataTableType) {

        }

        @Override
        public void addParameterType(ParameterTypeDefinition parameterType) {

        }

        @Override
        public void addDefaultParameterTransformer(DefaultParameterTransformerDefinition defaultParameterTransformer) {

        }

        @Override
        public void addDefaultDataTableCellTransformer(
                DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer
        ) {

        }

        @Override
        public void addDefaultDataTableEntryTransformer(
                DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer
        ) {

        }
    };

    @BeforeEach
    void setup() {
        LambdaGlueRegistry.INSTANCE.set(lambdaGlueRegistry);
    }

    @Test
    void testBeforeHook() {
        lambdaGlue.Before(this::hookNoArgs);
        assertHook(beforeHook, EMPTY_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER);
        lambdaGlue.Before("taxExpression", this::hookNoArgs);
        assertHook(beforeHook, "taxExpression", DEFAULT_BEFORE_ORDER);
        lambdaGlue.Before(42, this::hookNoArgs);
        assertHook(beforeHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.Before("taxExpression", 42, this::hookNoArgs);
        assertHook(beforeHook, "taxExpression", 42);

        lambdaGlue.Before(this::hook);
        assertHook(beforeHook, EMPTY_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER);
        lambdaGlue.Before("taxExpression", this::hook);
        assertHook(beforeHook, "taxExpression", DEFAULT_BEFORE_ORDER);
        lambdaGlue.Before(42, this::hook);
        assertHook(beforeHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.Before("taxExpression", 42, this::hook);
        assertHook(beforeHook, "taxExpression", 42);
    }

    void hookNoArgs() {
        invoked.set(true);
    }

    private void assertHook(HookDefinition hook, String tagExpression, int beforeOrder) {
        assertThat(hook.getTagExpression(), is(tagExpression));
        assertThat(hook.getOrder(), is(beforeOrder));
        hook.execute(state);
        assertTrue(invoked.get());
        invoked.set(false);
    }

    void hook(Scenario scenario) {
        invoked.set(true);
    }

    @Test
    void testBeforeStepHook() {
        lambdaGlue.BeforeStep(this::hookNoArgs);
        assertHook(beforeStepHook, EMPTY_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER);
        lambdaGlue.BeforeStep("taxExpression", this::hookNoArgs);
        assertHook(beforeStepHook, "taxExpression", DEFAULT_BEFORE_ORDER);
        lambdaGlue.BeforeStep(42, this::hookNoArgs);
        assertHook(beforeStepHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.BeforeStep("taxExpression", 42, this::hookNoArgs);
        assertHook(beforeStepHook, "taxExpression", 42);

        lambdaGlue.BeforeStep(this::hook);
        assertHook(beforeStepHook, EMPTY_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER);
        lambdaGlue.BeforeStep("taxExpression", this::hook);
        assertHook(beforeStepHook, "taxExpression", DEFAULT_BEFORE_ORDER);
        lambdaGlue.BeforeStep(42, this::hook);
        assertHook(beforeStepHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.BeforeStep("taxExpression", 42, this::hook);
        assertHook(beforeStepHook, "taxExpression", 42);
    }

    @Test
    void testAfterHook() {
        lambdaGlue.After(this::hookNoArgs);
        assertHook(afterHook, EMPTY_TAG_EXPRESSION, DEFAULT_AFTER_ORDER);
        lambdaGlue.After("taxExpression", this::hookNoArgs);
        assertHook(afterHook, "taxExpression", DEFAULT_AFTER_ORDER);
        lambdaGlue.After(42, this::hookNoArgs);
        assertHook(afterHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.After("taxExpression", 42, this::hookNoArgs);
        assertHook(afterHook, "taxExpression", 42);

        lambdaGlue.After(this::hook);
        assertHook(afterHook, EMPTY_TAG_EXPRESSION, DEFAULT_AFTER_ORDER);
        lambdaGlue.After("taxExpression", this::hook);
        assertHook(afterHook, "taxExpression", DEFAULT_AFTER_ORDER);
        lambdaGlue.After(42, this::hook);
        assertHook(afterHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.After("taxExpression", 42, this::hook);
        assertHook(afterHook, "taxExpression", 42);
    }

    @Test
    void testAfterStepHook() {
        lambdaGlue.AfterStep(this::hookNoArgs);
        assertHook(afterStepHook, EMPTY_TAG_EXPRESSION, DEFAULT_AFTER_ORDER);
        lambdaGlue.AfterStep("taxExpression", this::hookNoArgs);
        assertHook(afterStepHook, "taxExpression", DEFAULT_AFTER_ORDER);
        lambdaGlue.AfterStep(42, this::hookNoArgs);
        assertHook(afterStepHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.AfterStep("taxExpression", 42, this::hookNoArgs);
        assertHook(afterStepHook, "taxExpression", 42);

        lambdaGlue.AfterStep(this::hook);
        assertHook(afterStepHook, EMPTY_TAG_EXPRESSION, DEFAULT_AFTER_ORDER);
        lambdaGlue.AfterStep("taxExpression", this::hook);
        assertHook(afterStepHook, "taxExpression", DEFAULT_AFTER_ORDER);
        lambdaGlue.AfterStep(42, this::hook);
        assertHook(afterStepHook, EMPTY_TAG_EXPRESSION, 42);
        lambdaGlue.AfterStep("taxExpression", 42, this::hook);
        assertHook(afterStepHook, "taxExpression", 42);
    }

}
