package io.cucumber.java8;

import io.cucumber.java8.StepdefBody.A0;
import io.cucumber.java8.StepdefBody.A1;
import io.cucumber.java8.StepdefBody.A2;
import io.cucumber.java8.StepdefBody.A3;
import io.cucumber.java8.StepdefBody.A4;
import io.cucumber.java8.StepdefBody.A5;
import io.cucumber.java8.StepdefBody.A6;
import io.cucumber.java8.StepdefBody.A7;
import io.cucumber.java8.StepdefBody.A8;
import io.cucumber.java8.StepdefBody.A9;

import io.cucumber.java8.LambdaGlueRegistry;
import io.cucumber.java8.Java8StepDefinition;
import io.cucumber.java8.LambdaGlue;

import org.apiguardian.api.API;

/**
 * ${locale.getDisplayLanguage()}
 * <p>
 * To execute steps in a feature file the steps must be
 * connected to executable code. This can be done by
 * implementing this interface.
 * <p>
 * The parameters extracted from the step by the expression
 * along with the data table or doc string argument are provided as
 * arguments to the lambda expression.
 * <p>
 * The types of the parameters are determined by the cucumber or
 * regular expression.
 * <p>
 * The type of the data table or doc string argument is determined
 * by the argument name value. When none is provided cucumber will
 * attempt to transform the data table or doc string to the the
 * type of last argument.
 */
@API(status = API.Status.STABLE)
public interface ${className} extends LambdaGlue {
<% i18n.stepKeywords.findAll { !it.contains('*') && !it.matches("^\\d.*") }.sort().unique().each { kw -> %>
    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with no parameters
     */
    default void ${java.text.Normalizer.normalize(kw.replaceAll("[\\s',!]", ""), java.text.Normalizer.Form.NFC)}(String expression, A0 body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A0.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * The timeout controls how long step is allowed to run. Cucumber
     * will mark the step as failed when exceeded. When the maximum
     * duration is exceeded the thread will receive an in interrupt.
     * Note: if the interrupt is ignored cucumber will wait for the this
     * step to finish.
     *
     * @param expression    the cucumber expression
     * @param timeoutMillis timeout in milliseconds. 0 (default) means no restriction.
     * @param body          a lambda expression with no parameters
     * @deprecated use a library based solution instead. E.g. Awaitility
     * or JUnit 5s Assertions.assertTimeout.
     */
    @Deprecated
    default void ${java.text.Normalizer.normalize(kw.replaceAll("[\\s',!]", ""), java.text.Normalizer.Form.NFC)}(String expression, long timeoutMillis, A0 body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, timeoutMillis, A0.class, body));
    }

    <% (1..9).each { arity ->
      def ts = (1..arity).collect { n -> "T"+n }
      def genericSignature = ts.join(",") %>
    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with ${arity} parameters
     * <% (1..arity).each { i -> %>
     * @param <T${i}> type of argument ${i} <% } %>
     */
    default <${genericSignature}> void ${java.text.Normalizer.normalize(kw.replaceAll("[\\s',!]", ""), java.text.Normalizer.Form.NFC)}(String expression, A${arity}<${genericSignature}> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A${arity}.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * The timeout controls how long step is allowed to run. Cucumber
     * will mark the step as failed when exceeded. When the maximum
     * duration is exceeded the thread will receive an in interrupt.
     * Note: if the interrupt is ignored cucumber will wait for the this
     * step to finish.
     *
     * @param expression    the cucumber expression
     * @param timeoutMillis timeout in milliseconds. 0 (default) means no restriction.
     * @param body          a lambda expression with ${arity} parameters
     * <% (1..arity).each { i -> %>
     * @param <T${i}> type of argument ${i} <% } %>
     * @deprecated use a library based solution instead. E.g. Awaitility
     * or JUnit 5s Assertions.assertTimeout.
     */
    @Deprecated
    default <${genericSignature}> void ${java.text.Normalizer.normalize(kw.replaceAll("[\\s',!]", ""), java.text.Normalizer.Form.NFC)}(String expression, long timeoutMillis, A${arity}<${genericSignature}> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, timeoutMillis, A${arity}.class, body));
    }

    <% } %>
<% } %>
}
