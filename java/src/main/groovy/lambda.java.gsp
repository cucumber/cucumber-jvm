package io.cucumber.java.api.lambda;

import io.cucumber.java.api.StepdefBody.A0;
import io.cucumber.java.api.StepdefBody.A1;
import io.cucumber.java.api.StepdefBody.A2;
import io.cucumber.java.api.StepdefBody.A3;
import io.cucumber.java.api.StepdefBody.A4;
import io.cucumber.java.api.StepdefBody.A5;
import io.cucumber.java.api.StepdefBody.A6;
import io.cucumber.java.api.StepdefBody.A7;
import io.cucumber.java.api.StepdefBody.A8;
import io.cucumber.java.api.StepdefBody.A9;

import io.cucumber.java.LambdaGlueRegistry;
import io.cucumber.java.Java8StepDefinition;
import io.cucumber.java.LambdaGlueBase;

/**
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
public interface ${className} extends LambdaGlueBase {
<% i18n.stepKeywords.findAll { !it.contains('*') && !it.matches("^\\d.*") }.sort().unique().each { kw -> %>
    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with no parameters
     */
    default void ${java.text.Normalizer.normalize(kw.replaceAll("[\\s',!]", ""), java.text.Normalizer.Form.NFC)}(String expression, A0 body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition((typeRegistry) ->
            Java8StepDefinition.create(expression, A0.class, body, typeRegistry)
        );
    }

    /**
     * Creates a new step definition.
     *
     * @param expression    the cucumber expression
     * @param timeoutMillis max amount of milliseconds this is allowed to run for. 0 (default) means no restriction.
     * @param body          a lambda expression with no parameters
     */
    default void ${java.text.Normalizer.normalize(kw.replaceAll("[\\s',!]", ""), java.text.Normalizer.Form.NFC)}(String expression, long timeoutMillis, A0 body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition((typeRegistry) ->
            Java8StepDefinition.create(expression, timeoutMillis, A0.class, body, typeRegistry)
        );
    }

    <% (1..9).each { arity ->
      def ts = (1..arity).collect { n -> "T"+n }
      def genericSignature = ts.join(",") %>    
    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with ${arity} parameters
     */      
    default <${genericSignature}> void ${java.text.Normalizer.normalize(kw.replaceAll("[\\s',!]", ""), java.text.Normalizer.Form.NFC)}(String expression, A${arity}<${genericSignature}> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition((typeRegistry) ->
            Java8StepDefinition.create(expression, A${arity}.class, body, typeRegistry)
        );
    }

    /**
     * Creates a new step definition.
     *
     * @param expression    the cucumber expression
     * @param timeoutMillis max amount of milliseconds this is allowed to run for. 0 (default) means no restriction.
     * @param body          a lambda expression with ${arity} parameters
     */
    default <${genericSignature}> void ${java.text.Normalizer.normalize(kw.replaceAll("[\\s',!]", ""), java.text.Normalizer.Form.NFC)}(String expression, long timeoutMillis, A${arity}<${genericSignature}> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition((typeRegistry) ->
            Java8StepDefinition.create(expression, timeoutMillis, A${arity}.class, body, typeRegistry)
        );
    }

    <% } %>
<% } %>
}
