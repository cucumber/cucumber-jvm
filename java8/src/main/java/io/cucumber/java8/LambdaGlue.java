package io.cucumber.java8;

import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionException;
import io.cucumber.tagexpressions.TagExpressionParser;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public interface LambdaGlue {

    final String[] NO_REPLACEMENT = {};
    final String EMPTY_TAG_EXPRESSION = "";
    final Expression EMPTY_PARSED_TAG_EXPRESSION = TagExpressionParser.parse(EMPTY_TAG_EXPRESSION);
    final int DEFAULT_BEFORE_ORDER = 1000;
    final int DEFAULT_AFTER_ORDER = 1000;

    /**
     * Defines an before hook.
     *
     * @param body lambda to execute, takes {@link Scenario} as an argument
     */
    default void Before(final HookBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addBeforeHookDefinition(
                    new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER, body));
    }

    /**
     * Defines an before hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param body          lambda to execute, takes {@link Scenario} as an
     *                      argument
     */
    default void Before(String tagExpression, final HookBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get()
                    .addBeforeHookDefinition(
                        new Java8HookDefinition(TagExpressionParser.parse(tagExpression), DEFAULT_BEFORE_ORDER, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format(" at '%s'", body.getClass().getEnclosingClass().getName()), tee);
        }
    }

    /**
     * Defines an before hook.
     *
     * @param order the order in which this hook should run. Higher numbers are
     *              run first
     * @param body  lambda to execute, takes {@link Scenario} as an argument
     */
    default void Before(int order, final HookBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addBeforeHookDefinition(new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, order, body));
    }

    /**
     * Defines an before hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param order         the order in which this hook should run. Higher
     *                      numbers are run first
     * @param body          lambda to execute, takes {@link Scenario} as an
     *                      argument
     */
    default void Before(String tagExpression, int order, final HookBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get().addBeforeHookDefinition(
                new Java8HookDefinition(TagExpressionParser.parse(tagExpression), order, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format("at '%s'", body.getClass().getEnclosingClass().getName()), tee);
        }
    }

    /**
     * Defines an before hook.
     *
     * @param body lambda to execute, takes {@link Scenario} as an argument
     */
    default void Before(final HookNoArgsBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addBeforeHookDefinition(
                    new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER, body));
    }

    /**
     * Defines an before hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param body          lambda to execute
     */
    default void Before(String tagExpression, final HookNoArgsBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get()
                    .addBeforeHookDefinition(
                        new Java8HookDefinition(TagExpressionParser.parse(tagExpression), DEFAULT_BEFORE_ORDER, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format(" at '%s'", body.getClass().getEnclosingClass().getName()), tee);
        }
    }

    /**
     * Defines an before hook.
     *
     * @param order the order in which this hook should run. Higher numbers are
     *              run first
     * @param body  lambda to execute
     */
    default void Before(int order, final HookNoArgsBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addBeforeHookDefinition(new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, order, body));
    }

    /**
     * Defines an before hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param order         the order in which this hook should run. Higher
     *                      numbers are run first
     * @param body          lambda to execute
     */
    default void Before(String tagExpression, int order, final HookNoArgsBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get().addBeforeHookDefinition(
                new Java8HookDefinition(TagExpressionParser.parse(tagExpression), order, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format(" at 'Before'", body.getClass().getEnclosingClass().getName()), tee);
        }
    }

    /**
     * Defines an before step hook.
     *
     * @param body lambda to execute, takes {@link Scenario} as an argument
     */
    default void BeforeStep(final HookBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addBeforeStepHookDefinition(
                    new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER, body));
    }

    /**
     * Defines an before step hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param body          lambda to execute, takes {@link Scenario} as an
     *                      argument
     */
    default void BeforeStep(String tagExpression, final HookBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get()
                    .addBeforeStepHookDefinition(
                        new Java8HookDefinition(TagExpressionParser.parse(tagExpression), DEFAULT_BEFORE_ORDER, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format(" at '%s'", body.getClass().getEnclosingClass().getName()), tee);
        }
    }

    /**
     * Defines an before step hook.
     *
     * @param order the order in which this hook should run. Higher numbers are
     *              run first
     * @param body  lambda to execute, takes {@link Scenario} as an argument
     */
    default void BeforeStep(int order, final HookBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addBeforeStepHookDefinition(new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, order, body));
    }

    /**
     * Defines an before step hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param order         the order in which this hook should run. Higher
     *                      numbers are run first
     * @param body          lambda to execute, takes {@link Scenario} as an
     *                      argument
     */
    default void BeforeStep(String tagExpression, int order, final HookBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get()
                    .addBeforeStepHookDefinition(
                        new Java8HookDefinition(TagExpressionParser.parse(tagExpression), order, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(tee.toString() + String.format(" at '%s'", body.getClass().getEnclosingClass()),
                tee);
        }
    }

    /**
     * Defines an before step hook.
     *
     * @param body lambda to execute
     */
    default void BeforeStep(final HookNoArgsBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addBeforeStepHookDefinition(
                    new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, DEFAULT_BEFORE_ORDER, body));
    }

    /**
     * Defines an before step hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param body          lambda to execute
     */
    default void BeforeStep(String tagExpression, final HookNoArgsBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get()
                    .addBeforeStepHookDefinition(
                        new Java8HookDefinition(TagExpressionParser.parse(tagExpression), DEFAULT_BEFORE_ORDER, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format(" at '%s'", body.getClass().getEnclosingClass().getName()),
                tee);
        }
    }

    /**
     * Defines an before step hook.
     *
     * @param order the order in which this hook should run. Higher numbers are
     *              run first
     * @param body  lambda to execute
     */
    default void BeforeStep(int order, final HookNoArgsBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addBeforeStepHookDefinition(new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, order, body));
    }

    /**
     * Defines an before step hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param order         the order in which this hook should run. Higher
     *                      numbers are run first
     * @param body          lambda to execute
     */
    default void BeforeStep(String tagExpression, int order, final HookNoArgsBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get()
                    .addBeforeStepHookDefinition(
                        new Java8HookDefinition(TagExpressionParser.parse(tagExpression), order, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format(" at '%s'", body.getClass().getEnclosingClass().getName()), tee);
        }

    }

    /**
     * Defines an after hook.
     *
     * @param body lambda to execute, takes {@link Scenario} as an argument
     */
    default void After(final HookBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addAfterHookDefinition(
                    new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, DEFAULT_AFTER_ORDER, body));
    }

    /**
     * Defines an after hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param body          lambda to execute, takes {@link Scenario} as an
     *                      argument
     */
    default void After(String tagExpression, final HookBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get()
                    .addAfterHookDefinition(
                        new Java8HookDefinition(TagExpressionParser.parse(tagExpression), DEFAULT_AFTER_ORDER, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format(" at '%s'", body.getClass().getEnclosingClass().getName()), tee);
        }
    }

    /**
     * Defines an after hook.
     *
     * @param order the order in which this hook should run. Higher numbers are
     *              run first
     * @param body  lambda to execute, takes {@link Scenario} as an argument
     */
    default void After(int order, final HookBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addAfterHookDefinition(new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, order, body));
    }

    /**
     * Defines and after hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param order         the order in which this hook should run. Higher
     *                      numbers are run first
     * @param body          lambda to execute, takes {@link Scenario} as an
     *                      argument
     */
    default void After(String tagExpression, int order, final HookBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get().addAfterHookDefinition(
                new Java8HookDefinition(TagExpressionParser.parse(tagExpression), order, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format(" at '%s'", body.getClass().getEnclosingClass().getName()), tee);
        }
    }

    /**
     * Defines and after hook.
     *
     * @param body lambda to execute
     */
    default void After(final HookNoArgsBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addAfterHookDefinition(
                    new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, DEFAULT_AFTER_ORDER, body));
    }

    /**
     * Defines and after hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param body          lambda to execute
     */
    default void After(String tagExpression, final HookNoArgsBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get()
                    .addAfterHookDefinition(
                        new Java8HookDefinition(TagExpressionParser.parse(tagExpression), DEFAULT_AFTER_ORDER, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format(" at '%s'", body.getClass().getEnclosingClass().getName()), tee);
        }
    }

    /**
     * Defines and after hook.
     *
     * @param order the order in which this hook should run. Higher numbers are
     *              run first
     * @param body  lambda to execute
     */
    default void After(int order, final HookNoArgsBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addAfterHookDefinition(new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, order, body));
    }

    /**
     * Defines and after hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param order         the order in which this hook should run. Higher
     *                      numbers are run first
     * @param body          lambda to execute
     */
    default void After(String tagExpression, int order, final HookNoArgsBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get().addAfterHookDefinition(
                new Java8HookDefinition(TagExpressionParser.parse(tagExpression), order, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format(" at '%s'", body.getClass().getEnclosingClass().getName()), tee);
        }
    }

    /**
     * Defines and after step hook.
     *
     * @param body lambda to execute, takes {@link Scenario} as an argument
     */
    default void AfterStep(final HookBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addAfterStepHookDefinition(
                    new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, DEFAULT_AFTER_ORDER, body));
    }

    /**
     * Defines and after step hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param body          lambda to execute, takes {@link Scenario} as an
     *                      argument
     */
    default void AfterStep(String tagExpression, final HookBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get()
                    .addAfterStepHookDefinition(
                        new Java8HookDefinition(TagExpressionParser.parse(tagExpression), DEFAULT_AFTER_ORDER, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format(" at '%s'", body.getClass().getEnclosingClass().getName()), tee);
        }

    }

    /**
     * Defines and after step hook.
     *
     * @param order the order in which this hook should run. Higher numbers are
     *              run first
     * @param body  lambda to execute, takes {@link Scenario} as an argument
     */
    default void AfterStep(int order, final HookBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addAfterStepHookDefinition(new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, order, body));
    }

    /**
     * Defines and after step hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param order         the order in which this hook should run. Higher
     *                      numbers are run first
     * @param body          lambda to execute, takes {@link Scenario} as an
     *                      argument
     */
    default void AfterStep(String tagExpression, int order, final HookBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get()
                    .addAfterStepHookDefinition(
                        new Java8HookDefinition(TagExpressionParser.parse(tagExpression), order, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format(" at '%s'", body.getClass().getEnclosingClass().getName()), tee);
        }
    }

    /**
     * Defines and after step hook.
     *
     * @param body lambda to execute
     */
    default void AfterStep(final HookNoArgsBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addAfterStepHookDefinition(
                    new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, DEFAULT_AFTER_ORDER, body));
    }

    /**
     * Defines and after step hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param body          lambda to execute
     */
    default void AfterStep(String tagExpression, final HookNoArgsBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get()
                    .addAfterStepHookDefinition(
                        new Java8HookDefinition(TagExpressionParser.parse(tagExpression), DEFAULT_AFTER_ORDER, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format(" at '%s'", body.getClass().getEnclosingClass().getName()), tee);
        }
    }

    /**
     * Defines and after step hook.
     *
     * @param order the order in which this hook should run. Higher numbers are
     *              run first
     * @param body  lambda to execute
     */
    default void AfterStep(int order, final HookNoArgsBody body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addAfterStepHookDefinition(new Java8HookDefinition(EMPTY_PARSED_TAG_EXPRESSION, order, body));
    }

    /**
     * Defines and after step hook.
     *
     * @param tagExpression a tag expression, if the expression applies to the
     *                      current scenario this hook will be executed
     * @param order         the order in which this hook should run. Higher
     *                      numbers are run first
     * @param body          lambda to execute
     */
    default void AfterStep(String tagExpression, int order, final HookNoArgsBody body) {
        try {
            LambdaGlueRegistry.INSTANCE.get()
                    .addAfterStepHookDefinition(
                        new Java8HookDefinition(TagExpressionParser.parse(tagExpression), order, body));
        } catch (TagExpressionException tee) {
            throw new RuntimeException(
                tee.toString() + String.format(" at '%s'", body.getClass().getEnclosingClass().getName()), tee);
        }
    }

    /**
     * Register doc string type.
     *
     * @param contentType Name of the content type.
     * @param body        a function that creates an instance of
     *                    <code>type</code> from the doc string
     * @see               io.cucumber.docstring.DocStringType
     */
    default void DocStringType(String contentType, DocStringDefinitionBody<?> body) {
        LambdaGlueRegistry.INSTANCE.get().addDocStringType(new Java8DocStringTypeDefinition(contentType, body));
    }

    /**
     * Register a data table type.
     *
     * @param <T>  the data table type
     * @param body a function that creates an instance of <code>type</code> from
     *             the data table
     */
    default <T> void DataTableType(DataTableEntryDefinitionBody<T> body) {
        LambdaGlueRegistry.INSTANCE.get().addDataTableType(new Java8DataTableEntryDefinition(NO_REPLACEMENT, body));
    }

    /**
     * Register a data table type with a replacement.
     * <p>
     * A data table can only represent absent and non-empty strings. By
     * replacing a known value (for example [empty]) a data table can also
     * represent empty strings.
     *
     * @param <T>                    the data table type
     * @param replaceWithEmptyString a string that will be replaced with an
     *                               empty string.
     * @param body                   a function that creates an instance of
     *                               <code>type</code> from the data table
     */
    default <T> void DataTableType(String replaceWithEmptyString, DataTableEntryDefinitionBody<T> body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addDataTableType(new Java8DataTableEntryDefinition(new String[] { replaceWithEmptyString }, body));
    }

    /**
     * Register a data table type
     *
     * @param body a function that creates an instance of <code>type</code> from
     *             the data table
     * @param <T>  the data table type
     */
    default <T> void DataTableType(DataTableRowDefinitionBody<T> body) {
        LambdaGlueRegistry.INSTANCE.get().addDataTableType(new Java8DataTableRowDefinition(NO_REPLACEMENT, body));
    }

    /**
     * Register a data table type with a replacement.
     * <p>
     * A data table can only represent absent and non-empty strings. By
     * replacing a known value (for example [empty]) a data table can also
     * represent empty strings.
     *
     * @param <T>                    the data table type
     * @param replaceWithEmptyString a string that will be replaced with an
     *                               empty string.
     * @param body                   a function that creates an instance of
     *                               <code>type</code> from the data table
     */
    default <T> void DataTableType(String replaceWithEmptyString, DataTableRowDefinitionBody<T> body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addDataTableType(new Java8DataTableRowDefinition(new String[] { replaceWithEmptyString }, body));
    }

    /**
     * Register a data table type
     *
     * @param body a function that creates an instance of <code>type</code> from
     *             the data table
     * @param <T>  the data table type
     */
    default <T> void DataTableType(DataTableCellDefinitionBody<T> body) {
        LambdaGlueRegistry.INSTANCE.get().addDataTableType(new Java8DataTableCellDefinition(NO_REPLACEMENT, body));
    }

    /**
     * Register a data table type with a replacement.
     * <p>
     * A data table can only represent absent and non-empty strings. By
     * replacing a known value (for example [empty]) a data table can also
     * represent empty strings.
     *
     * @param <T>                    the data table type
     * @param replaceWithEmptyString a string that will be replaced with an
     *                               empty string.
     * @param body                   a function that creates an instance of
     *                               <code>type</code> from the data table
     */
    default <T> void DataTableType(String replaceWithEmptyString, DataTableCellDefinitionBody<T> body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addDataTableType(new Java8DataTableCellDefinition(new String[] { replaceWithEmptyString }, body));
    }

    /**
     * Register a data table type
     *
     * @param body a function that creates an instance of <code>type</code> from
     *             the data table
     * @param <T>  the data table type
     */
    default <T> void DataTableType(DataTableDefinitionBody<T> body) {
        LambdaGlueRegistry.INSTANCE.get().addDataTableType(new Java8DataTableDefinition(NO_REPLACEMENT, body));
    }

    /**
     * Register a data table type with a replacement.
     * <p>
     * A data table can only represent absent and non-empty strings. By
     * replacing a known value (for example [empty]) a data table can also
     * represent empty strings.
     *
     * @param <T>                    the data table type
     * @param replaceWithEmptyString a string that will be replaced with an
     *                               empty string.
     * @param body                   a function that creates an instance of
     *                               <code>type</code> from the data table
     */
    default <T> void DataTableType(String replaceWithEmptyString, DataTableDefinitionBody<T> body) {
        LambdaGlueRegistry.INSTANCE.get()
                .addDataTableType(new Java8DataTableDefinition(new String[] { replaceWithEmptyString }, body));
    }

    /**
     * Register parameter type.
     *
     * @param <R>            the parameter type
     *                       {@link io.cucumber.cucumberexpressions.ParameterType#getType()}
     * @param name           used as the type name in typed expressions
     *                       {@link io.cucumber.cucumberexpressions.ParameterType#getName()}
     * @param regex          expression to match
     * @param definitionBody converts {@code String} argument to the target
     *                       parameter type
     * @see                  io.cucumber.cucumberexpressions.ParameterType
     * @see                  <a
     *                       href=https://cucumber.io/docs/cucumber/cucumber-expressions>Cucumber
     *                       Expressions</a>
     */
    default <R> void ParameterType(String name, String regex, ParameterDefinitionBody.A1<R> definitionBody) {
        LambdaGlueRegistry.INSTANCE.get().addParameterType(
            new Java8ParameterTypeDefinition(name, regex, ParameterDefinitionBody.A1.class, definitionBody));
    }

    /**
     * Register parameter type.
     *
     * @param <R>            the parameter type.
     *                       {@link io.cucumber.cucumberexpressions.ParameterType#getType()}
     * @param name           used as the type name in typed expressions.
     *                       {@link io.cucumber.cucumberexpressions.ParameterType#getName()}
     * @param regex          expression to match. If the expression includes
     *                       capture groups their captured strings will be
     *                       provided as individual arguments.
     * @param definitionBody converts {@code String} arguments to the target
     *                       parameter type
     * @see                  io.cucumber.cucumberexpressions.ParameterType
     * @see                  <a
     *                       href=https://cucumber.io/docs/cucumber/cucumber-expressions>Cucumber
     *                       Expressions</a>
     */
    default <R> void ParameterType(String name, String regex, ParameterDefinitionBody.A2<R> definitionBody) {
        LambdaGlueRegistry.INSTANCE.get().addParameterType(
            new Java8ParameterTypeDefinition(name, regex, ParameterDefinitionBody.A2.class, definitionBody));
    }

    /**
     * @see LambdaGlue#ParameterType(String, String,
     *      io.cucumber.java8.ParameterDefinitionBody.A2)
     */
    default <R> void ParameterType(String name, String regex, ParameterDefinitionBody.A3<R> definitionBody) {
        LambdaGlueRegistry.INSTANCE.get().addParameterType(
            new Java8ParameterTypeDefinition(name, regex, ParameterDefinitionBody.A3.class, definitionBody));
    }

    /**
     * @see LambdaGlue#ParameterType(String, String,
     *      io.cucumber.java8.ParameterDefinitionBody.A2)
     */
    default <R> void ParameterType(String name, String regex, ParameterDefinitionBody.A4<R> definitionBody) {
        LambdaGlueRegistry.INSTANCE.get().addParameterType(
            new Java8ParameterTypeDefinition(name, regex, ParameterDefinitionBody.A4.class, definitionBody));
    }

    /**
     * @see LambdaGlue#ParameterType(String, String,
     *      io.cucumber.java8.ParameterDefinitionBody.A2)
     */
    default <R> void ParameterType(String name, String regex, ParameterDefinitionBody.A5<R> definitionBody) {
        LambdaGlueRegistry.INSTANCE.get().addParameterType(
            new Java8ParameterTypeDefinition(name, regex, ParameterDefinitionBody.A5.class, definitionBody));
    }

    /**
     * @see LambdaGlue#ParameterType(String, String,
     *      io.cucumber.java8.ParameterDefinitionBody.A2)
     */
    default <R> void ParameterType(String name, String regex, ParameterDefinitionBody.A6<R> definitionBody) {
        LambdaGlueRegistry.INSTANCE.get().addParameterType(
            new Java8ParameterTypeDefinition(name, regex, ParameterDefinitionBody.A6.class, definitionBody));
    }

    /**
     * @see LambdaGlue#ParameterType(String, String,
     *      io.cucumber.java8.ParameterDefinitionBody.A2)
     */
    default <R> void ParameterType(String name, String regex, ParameterDefinitionBody.A7<R> definitionBody) {
        LambdaGlueRegistry.INSTANCE.get().addParameterType(
            new Java8ParameterTypeDefinition(name, regex, ParameterDefinitionBody.A7.class, definitionBody));
    }

    /**
     * @see LambdaGlue#ParameterType(String, String,
     *      io.cucumber.java8.ParameterDefinitionBody.A2)
     */
    default <R> void ParameterType(String name, String regex, ParameterDefinitionBody.A8<R> definitionBody) {
        LambdaGlueRegistry.INSTANCE.get().addParameterType(
            new Java8ParameterTypeDefinition(name, regex, ParameterDefinitionBody.A8.class, definitionBody));
    }

    /**
     * @see LambdaGlue#ParameterType(String, String,
     *      io.cucumber.java8.ParameterDefinitionBody.A2)
     */
    default <R> void ParameterType(String name, String regex, ParameterDefinitionBody.A9<R> definitionBody) {
        LambdaGlueRegistry.INSTANCE.get().addParameterType(
            new Java8ParameterTypeDefinition(name, regex, ParameterDefinitionBody.A9.class, definitionBody));
    }

    /**
     * Register default parameter type transformer.
     *
     * @param definitionBody converts {@code String} argument to an instance of
     *                       the {@code Type} argument
     */
    default void DefaultParameterTransformer(DefaultParameterTransformerBody definitionBody) {
        LambdaGlueRegistry.INSTANCE.get()
                .addDefaultParameterTransformer(new Java8DefaultParameterTypeDefinition(definitionBody));
    }

    /**
     * Register default data table cell transformer.
     *
     * @param definitionBody converts {@code String} argument to an instance of
     *                       the {@code Type} argument
     */
    default void DefaultDataTableCellTransformer(DefaultDataTableCellTransformerBody definitionBody) {
        LambdaGlueRegistry.INSTANCE.get().addDefaultDataTableCellTransformer(
            new Java8DefaultDataTableCellTransformerDefinition(NO_REPLACEMENT, definitionBody));
    }

    /**
     * Register default data table cell transformer with a replacement.
     * <p>
     * A data table can only represent absent and non-empty strings. By
     * replacing a known value (for example [empty]) a data table can also
     * represent empty strings. *
     *
     * @param replaceWithEmptyString a string that will be replaced with an
     *                               empty string.
     * @param definitionBody         converts {@code String} argument to an
     *                               instance of the {@code Type} argument
     */
    default <T> void DefaultDataTableCellTransformer(
            String replaceWithEmptyString, DefaultDataTableCellTransformerBody definitionBody
    ) {
        LambdaGlueRegistry.INSTANCE.get().addDefaultDataTableCellTransformer(
            new Java8DefaultDataTableCellTransformerDefinition(new String[] { replaceWithEmptyString },
                definitionBody));
    }

    /**
     * Register default data table entry transformer.
     *
     * @param definitionBody converts {@code Map<String,String>} argument to an
     *                       instance of the {@code Type} argument
     */
    default void DefaultDataTableEntryTransformer(DefaultDataTableEntryTransformerBody definitionBody) {
        LambdaGlueRegistry.INSTANCE.get().addDefaultDataTableEntryTransformer(
            new Java8DefaultDataTableEntryTransformerDefinition(NO_REPLACEMENT, definitionBody));
    }

    /**
     * Register default data table cell transformer with a replacement.
     * <p>
     * A data table can only represent absent and non-empty strings. By
     * replacing a known value (for example [empty]) a data table can also
     * represent empty strings.
     *
     * @param replaceWithEmptyString a string that will be replaced with an
     *                               empty string.
     * @param definitionBody         converts {@code Map<String,String>}
     *                               argument to an instance of the {@code Type}
     *                               argument
     */
    default <T> void DefaultDataTableEntryTransformer(
            String replaceWithEmptyString, DefaultDataTableEntryTransformerBody definitionBody
    ) {
        LambdaGlueRegistry.INSTANCE.get().addDefaultDataTableEntryTransformer(
            new Java8DefaultDataTableEntryTransformerDefinition(new String[] { replaceWithEmptyString },
                definitionBody));
    }

}
