package io.cucumber.java8;

import io.cucumber.java8.StepDefinitionBody.A0;
import io.cucumber.java8.StepDefinitionBody.A1;
import io.cucumber.java8.StepDefinitionBody.A2;
import io.cucumber.java8.StepDefinitionBody.A3;
import io.cucumber.java8.StepDefinitionBody.A4;
import io.cucumber.java8.StepDefinitionBody.A5;
import io.cucumber.java8.StepDefinitionBody.A6;
import io.cucumber.java8.StepDefinitionBody.A7;
import io.cucumber.java8.StepDefinitionBody.A8;
import io.cucumber.java8.StepDefinitionBody.A9;
import org.apiguardian.api.API;

/**
 * Telugu - తెలుగు
 * <p>
 * To execute steps in a feature file the steps must be connected to executable
 * code. This can be done by implementing this interface.
 * <p>
 * The parameters extracted from the step by the expression along with the data
 * table or doc string argument are provided as arguments to the lambda
 * expression.
 * <p>
 * The types of the parameters are determined by the cucumber or regular
 * expression.
 * <p>
 * The type of the data table or doc string argument is determined by the
 * argument name value. When none is provided cucumber will attempt to transform
 * the data table or doc string to the the type of last argument.
 * 
 * @deprecated moved to {@code io.cucumber.java8.Te}
 */
@API(status = API.Status.STABLE)
@Deprecated
public interface Tl extends LambdaGlue {

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with no parameters
     */
    default void అప్పుడు(String expression, A0 body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A0.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 1 parameters
     * @param <T1>       type of argument 1
     */
    default <T1> void అప్పుడు(String expression, A1<T1> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A1.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 2 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     */
    default <T1, T2> void అప్పుడు(String expression, A2<T1, T2> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A2.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 3 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     */
    default <T1, T2, T3> void అప్పుడు(String expression, A3<T1, T2, T3> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A3.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 4 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     */
    default <T1, T2, T3, T4> void అప్పుడు(String expression, A4<T1, T2, T3, T4> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A4.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 5 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     */
    default <T1, T2, T3, T4, T5> void అప్పుడు(String expression, A5<T1, T2, T3, T4, T5> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A5.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 6 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     */
    default <T1, T2, T3, T4, T5, T6> void అప్పుడు(String expression, A6<T1, T2, T3, T4, T5, T6> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A6.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 7 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     */
    default <T1, T2, T3, T4, T5, T6, T7> void అప్పుడు(String expression, A7<T1, T2, T3, T4, T5, T6, T7> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A7.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 8 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     * @param <T8>       type of argument 8
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8> void అప్పుడు(String expression, A8<T1, T2, T3, T4, T5, T6, T7, T8> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A8.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 9 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     * @param <T8>       type of argument 8
     * @param <T9>       type of argument 9
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9> void అప్పుడు(
            String expression, A9<T1, T2, T3, T4, T5, T6, T7, T8, T9> body
    ) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A9.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with no parameters
     */
    default void ఈపరిస్థితిలో(String expression, A0 body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A0.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 1 parameters
     * @param <T1>       type of argument 1
     */
    default <T1> void ఈపరిస్థితిలో(String expression, A1<T1> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A1.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 2 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     */
    default <T1, T2> void ఈపరిస్థితిలో(String expression, A2<T1, T2> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A2.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 3 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     */
    default <T1, T2, T3> void ఈపరిస్థితిలో(String expression, A3<T1, T2, T3> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A3.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 4 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     */
    default <T1, T2, T3, T4> void ఈపరిస్థితిలో(String expression, A4<T1, T2, T3, T4> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A4.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 5 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     */
    default <T1, T2, T3, T4, T5> void ఈపరిస్థితిలో(String expression, A5<T1, T2, T3, T4, T5> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A5.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 6 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     */
    default <T1, T2, T3, T4, T5, T6> void ఈపరిస్థితిలో(String expression, A6<T1, T2, T3, T4, T5, T6> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A6.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 7 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     */
    default <T1, T2, T3, T4, T5, T6, T7> void ఈపరిస్థితిలో(String expression, A7<T1, T2, T3, T4, T5, T6, T7> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A7.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 8 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     * @param <T8>       type of argument 8
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8> void ఈపరిస్థితిలో(
            String expression, A8<T1, T2, T3, T4, T5, T6, T7, T8> body
    ) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A8.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 9 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     * @param <T8>       type of argument 8
     * @param <T9>       type of argument 9
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9> void ఈపరిస్థితిలో(
            String expression, A9<T1, T2, T3, T4, T5, T6, T7, T8, T9> body
    ) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A9.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with no parameters
     */
    default void కాని(String expression, A0 body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A0.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 1 parameters
     * @param <T1>       type of argument 1
     */
    default <T1> void కాని(String expression, A1<T1> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A1.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 2 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     */
    default <T1, T2> void కాని(String expression, A2<T1, T2> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A2.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 3 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     */
    default <T1, T2, T3> void కాని(String expression, A3<T1, T2, T3> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A3.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 4 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     */
    default <T1, T2, T3, T4> void కాని(String expression, A4<T1, T2, T3, T4> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A4.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 5 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     */
    default <T1, T2, T3, T4, T5> void కాని(String expression, A5<T1, T2, T3, T4, T5> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A5.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 6 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     */
    default <T1, T2, T3, T4, T5, T6> void కాని(String expression, A6<T1, T2, T3, T4, T5, T6> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A6.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 7 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     */
    default <T1, T2, T3, T4, T5, T6, T7> void కాని(String expression, A7<T1, T2, T3, T4, T5, T6, T7> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A7.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 8 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     * @param <T8>       type of argument 8
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8> void కాని(String expression, A8<T1, T2, T3, T4, T5, T6, T7, T8> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A8.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 9 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     * @param <T8>       type of argument 8
     * @param <T9>       type of argument 9
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9> void కాని(
            String expression, A9<T1, T2, T3, T4, T5, T6, T7, T8, T9> body
    ) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A9.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with no parameters
     */
    default void చెప్పబడినది(String expression, A0 body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A0.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 1 parameters
     * @param <T1>       type of argument 1
     */
    default <T1> void చెప్పబడినది(String expression, A1<T1> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A1.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 2 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     */
    default <T1, T2> void చెప్పబడినది(String expression, A2<T1, T2> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A2.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 3 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     */
    default <T1, T2, T3> void చెప్పబడినది(String expression, A3<T1, T2, T3> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A3.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 4 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     */
    default <T1, T2, T3, T4> void చెప్పబడినది(String expression, A4<T1, T2, T3, T4> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A4.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 5 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     */
    default <T1, T2, T3, T4, T5> void చెప్పబడినది(String expression, A5<T1, T2, T3, T4, T5> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A5.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 6 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     */
    default <T1, T2, T3, T4, T5, T6> void చెప్పబడినది(String expression, A6<T1, T2, T3, T4, T5, T6> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A6.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 7 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     */
    default <T1, T2, T3, T4, T5, T6, T7> void చెప్పబడినది(String expression, A7<T1, T2, T3, T4, T5, T6, T7> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A7.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 8 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     * @param <T8>       type of argument 8
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8> void చెప్పబడినది(
            String expression, A8<T1, T2, T3, T4, T5, T6, T7, T8> body
    ) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A8.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 9 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     * @param <T8>       type of argument 8
     * @param <T9>       type of argument 9
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9> void చెప్పబడినది(
            String expression, A9<T1, T2, T3, T4, T5, T6, T7, T8, T9> body
    ) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A9.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with no parameters
     */
    default void మరియు(String expression, A0 body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A0.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 1 parameters
     * @param <T1>       type of argument 1
     */
    default <T1> void మరియు(String expression, A1<T1> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A1.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 2 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     */
    default <T1, T2> void మరియు(String expression, A2<T1, T2> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A2.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 3 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     */
    default <T1, T2, T3> void మరియు(String expression, A3<T1, T2, T3> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A3.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 4 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     */
    default <T1, T2, T3, T4> void మరియు(String expression, A4<T1, T2, T3, T4> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A4.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 5 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     */
    default <T1, T2, T3, T4, T5> void మరియు(String expression, A5<T1, T2, T3, T4, T5> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A5.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 6 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     */
    default <T1, T2, T3, T4, T5, T6> void మరియు(String expression, A6<T1, T2, T3, T4, T5, T6> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A6.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 7 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     */
    default <T1, T2, T3, T4, T5, T6, T7> void మరియు(String expression, A7<T1, T2, T3, T4, T5, T6, T7> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A7.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 8 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     * @param <T8>       type of argument 8
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8> void మరియు(String expression, A8<T1, T2, T3, T4, T5, T6, T7, T8> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A8.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 9 parameters
     * @param <T1>       type of argument 1
     * @param <T2>       type of argument 2
     * @param <T3>       type of argument 3
     * @param <T4>       type of argument 4
     * @param <T5>       type of argument 5
     * @param <T6>       type of argument 6
     * @param <T7>       type of argument 7
     * @param <T8>       type of argument 8
     * @param <T9>       type of argument 9
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9> void మరియు(
            String expression, A9<T1, T2, T3, T4, T5, T6, T7, T8, T9> body
    ) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A9.class, body));
    }

}
