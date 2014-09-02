package cucumber.runtime.java8;

import cucumber.api.java8.GlueBase;
import cucumber.api.java8.HookBody;
import cucumber.api.java8.HookNoArgsBody;
import cucumber.runtime.java.JavaBackend;

public interface LambdaGlueBase extends GlueBase {

    String[] EMPTY_TAG_EXPRESSIONS = new String[0];
    long NO_TIMEOUT = 0;
    int DEFAULT_BEFORE_ORDER = 0;
    int DEFAULT_AFTER_ORDER = 1000;

    default void Before(final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(body, EMPTY_TAG_EXPRESSIONS, NO_TIMEOUT, DEFAULT_BEFORE_ORDER);
    }

    default void Before(String[] tagExpressions, final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(body, tagExpressions, NO_TIMEOUT, DEFAULT_BEFORE_ORDER);
    }

    default void Before(long timeoutMillis, final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(body, EMPTY_TAG_EXPRESSIONS, timeoutMillis, DEFAULT_BEFORE_ORDER);
    }

    default void Before(int order, final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(body, EMPTY_TAG_EXPRESSIONS, NO_TIMEOUT, order);
    }

    default void Before(String[] tagExpressions, long timeoutMillis, int order, final HookBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(body, tagExpressions, timeoutMillis, order);
    }

    default void Before(final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(body, EMPTY_TAG_EXPRESSIONS, NO_TIMEOUT, DEFAULT_BEFORE_ORDER);
    }

    default void Before(String[] tagExpressions, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(body, tagExpressions, NO_TIMEOUT, DEFAULT_BEFORE_ORDER);
    }

    default void Before(long timeoutMillis, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(body, EMPTY_TAG_EXPRESSIONS, timeoutMillis, DEFAULT_BEFORE_ORDER);
    }

    default void Before(int order, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(body, EMPTY_TAG_EXPRESSIONS, NO_TIMEOUT, order);
    }

    default void Before(String[] tagExpressions, long timeoutMillis, int order, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addBeforeHookDefinition(body, tagExpressions, timeoutMillis, order);
    }

    default void After(final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(body, EMPTY_TAG_EXPRESSIONS, NO_TIMEOUT, DEFAULT_AFTER_ORDER);
    }

    default void After(String[] tagExpressions, final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(body, tagExpressions, NO_TIMEOUT, DEFAULT_AFTER_ORDER);
    }

    default void After(long timeoutMillis, final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(body, EMPTY_TAG_EXPRESSIONS, timeoutMillis, DEFAULT_AFTER_ORDER);
    }

    default void After(int order, final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(body, EMPTY_TAG_EXPRESSIONS, NO_TIMEOUT, order);
    }

    default void After(String[] tagExpressions, long timeoutMillis, int order, final HookBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(body, tagExpressions, timeoutMillis, order);
    }

    default void After(final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(body, EMPTY_TAG_EXPRESSIONS, NO_TIMEOUT, DEFAULT_AFTER_ORDER);
    }

    default void After(String[] tagExpressions, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(body, tagExpressions, NO_TIMEOUT, DEFAULT_AFTER_ORDER);
    }

    default void After(long timeoutMillis, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(body, EMPTY_TAG_EXPRESSIONS, timeoutMillis, DEFAULT_AFTER_ORDER);
    }

    default void After(int order, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(body, EMPTY_TAG_EXPRESSIONS, NO_TIMEOUT, order);
    }

    default void After(String[] tagExpressions, long timeoutMillis, int order, final HookNoArgsBody body) {
        JavaBackend.INSTANCE.get().addAfterHookDefinition(body, tagExpressions, timeoutMillis, order);
    }
}
