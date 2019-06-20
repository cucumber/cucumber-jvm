package cucumber.runtime.java8;

import cucumber.api.java8.GlueBase;
import cucumber.api.java8.HookBody;
import cucumber.api.java8.HookNoArgsBody;
import cucumber.runtime.java.JavaBackend;

@Deprecated
public interface LambdaGlueBase extends GlueBase {

    String[] EMPTY_TAG_EXPRESSIONS = new String[0];
    long NO_TIMEOUT = 0;
    int DEFAULT_BEFORE_ORDER = 0;
    int DEFAULT_AFTER_ORDER = 1000;

    default void Before(final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_BEFORE_ORDER, NO_TIMEOUT, body));
    }

    default void Before(String[] tagExpressions, final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(new Java8HookDefinition(tagExpressions, DEFAULT_BEFORE_ORDER, NO_TIMEOUT, body));
    }

    default void Before(long timeoutMillis, final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_BEFORE_ORDER, timeoutMillis, body));
    }

    default void Before(int order, final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, order, NO_TIMEOUT, body));
    }

    default void Before(String[] tagExpressions, long timeoutMillis, int order, final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(new Java8HookDefinition(tagExpressions, order, timeoutMillis, body));
    }

    default void Before(final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_BEFORE_ORDER, NO_TIMEOUT, body));
    }

    default void Before(String[] tagExpressions, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(new Java8HookDefinition(tagExpressions, DEFAULT_BEFORE_ORDER, NO_TIMEOUT, body));
    }

    default void Before(long timeoutMillis, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_BEFORE_ORDER, timeoutMillis, body));
    }

    default void Before(int order, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, order, NO_TIMEOUT, body));
    }

    default void Before(String[] tagExpressions, long timeoutMillis, int order, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(new Java8HookDefinition(tagExpressions, order, timeoutMillis, body));
    }

    default void BeforeStep(final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeStepHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_BEFORE_ORDER, NO_TIMEOUT, body));
    }

    default void BeforeStep(String[] tagExpressions, final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeStepHookDefinition(new Java8HookDefinition(tagExpressions, DEFAULT_BEFORE_ORDER, NO_TIMEOUT, body));
    }

    default void BeforeStep(long timeoutMillis, final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeStepHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_BEFORE_ORDER, timeoutMillis, body));
    }

    default void BeforeStep(int order, final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeStepHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, order, NO_TIMEOUT, body));
    }

    default void BeforeStep(String[] tagExpressions, long timeoutMillis, int order, final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeStepHookDefinition(new Java8HookDefinition(tagExpressions, order, timeoutMillis, body));
    }

    default void BeforeStep(final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeStepHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_BEFORE_ORDER, NO_TIMEOUT, body));
    }

    default void BeforeStep(String[] tagExpressions, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeStepHookDefinition(new Java8HookDefinition(tagExpressions, DEFAULT_BEFORE_ORDER, NO_TIMEOUT, body));
    }

    default void BeforeStep(long timeoutMillis, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeStepHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_BEFORE_ORDER, timeoutMillis, body));
    }

    default void BeforeStep(int order, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeStepHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, order, NO_TIMEOUT, body));
    }

    default void BeforeStep(String[] tagExpressions, long timeoutMillis, int order, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeStepHookDefinition(new Java8HookDefinition(tagExpressions, order, timeoutMillis, body));
    }

    default void After(final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_AFTER_ORDER, NO_TIMEOUT, body));
    }

    default void After(String[] tagExpressions, final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(new Java8HookDefinition(tagExpressions, DEFAULT_AFTER_ORDER, NO_TIMEOUT, body));
    }

    default void After(long timeoutMillis, final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_AFTER_ORDER, timeoutMillis, body));
    }

    default void After(int order, final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, order, NO_TIMEOUT, body));
    }

    default void After(String[] tagExpressions, long timeoutMillis, int order, final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(new Java8HookDefinition(tagExpressions, order, timeoutMillis, body));
    }

    default void After(final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_AFTER_ORDER, NO_TIMEOUT, body));
    }

    default void After(String[] tagExpressions, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(new Java8HookDefinition(tagExpressions, DEFAULT_AFTER_ORDER, NO_TIMEOUT, body));
    }

    default void After(long timeoutMillis, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_AFTER_ORDER, timeoutMillis, body));
    }

    default void After(int order, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, order, NO_TIMEOUT, body));
    }

    default void After(String[] tagExpressions, long timeoutMillis, int order, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(new Java8HookDefinition(tagExpressions, order, timeoutMillis, body));
    }

    default void AfterStep(final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterStepHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_AFTER_ORDER, NO_TIMEOUT, body));
    }

    default void AfterStep(String[] tagExpressions, final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterStepHookDefinition(new Java8HookDefinition(tagExpressions, DEFAULT_AFTER_ORDER, NO_TIMEOUT, body));
    }

    default void AfterStep(long timeoutMillis, final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterStepHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_AFTER_ORDER, timeoutMillis, body));
    }

    default void AfterStep(int order, final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterStepHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, order, NO_TIMEOUT, body));
    }

    default void AfterStep(String[] tagExpressions, long timeoutMillis, int order, final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterStepHookDefinition(new Java8HookDefinition(tagExpressions, order, timeoutMillis, body));
    }

    default void AfterStep(final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterStepHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_AFTER_ORDER, NO_TIMEOUT, body));
    }

    default void AfterStep(String[] tagExpressions, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterStepHookDefinition(new Java8HookDefinition(tagExpressions, DEFAULT_AFTER_ORDER, NO_TIMEOUT, body));
    }

    default void AfterStep(long timeoutMillis, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterStepHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, DEFAULT_AFTER_ORDER, timeoutMillis, body));
    }

    default void AfterStep(int order, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterStepHookDefinition(new Java8HookDefinition(EMPTY_TAG_EXPRESSIONS, order, NO_TIMEOUT, body));
    }

    default void AfterStep(String[] tagExpressions, long timeoutMillis, int order, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterStepHookDefinition(new Java8HookDefinition(tagExpressions, order, timeoutMillis, body));
    }

}
