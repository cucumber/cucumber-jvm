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

import io.cucumber.java8.LambdaGlueRegistry;
import io.cucumber.java8.Java8StepDefinition;
import io.cucumber.java8.LambdaGlue;

import org.apiguardian.api.API;

/**
 * ${language_name}
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
 * attempt to transform the data table or doc string to the
 * type of last argument.
 */
@API(status = API.Status.STABLE)
public interface ${className} extends LambdaGlue {
    <#list keywords as kw>

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with no parameters
     */
    default void ${kw}(String expression, A0 body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A0.class, body));
    }

    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with 1 parameter
     *
     * @param <T1> type of argument 1
     */
    default <T1> void ${kw}(String expression, A1<T1> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A1.class, body));
    }

    <#list 2..9 as arity>
<#-- TODO: use function or macro for genericSignature ? -->
    <#assign repeat = arity -1>
    /**
     * Creates a new step definition.
     *
     * @param expression the cucumber expression
     * @param body       a lambda expression with ${arity} parameters
     *
     <#list 1..arity as i>
     * @param <T${i}> type of argument ${i}
     </#list>
     */
    default <<#list 1..repeat as i>T${i},</#list>T${arity}> void ${kw}(String expression, A${arity}<<#list 1..repeat as i>T${i},</#list>T${arity}> body) {
        LambdaGlueRegistry.INSTANCE.get().addStepDefinition(Java8StepDefinition.create(expression, A${arity}.class, body));
    }

    </#list>
    </#list>
}
