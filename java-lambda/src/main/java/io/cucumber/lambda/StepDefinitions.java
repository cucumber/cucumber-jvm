package io.cucumber.lambda;

import io.cucumber.lambda.StepDefinitionFunction.C1A0;
import io.cucumber.lambda.StepDefinitionFunction.C1A1;
import io.cucumber.lambda.StepDefinitionFunction.C1A2;
import io.cucumber.lambda.StepDefinitionFunction.C1A3;
import io.cucumber.lambda.StepDefinitionFunction.C1A4;
import io.cucumber.lambda.StepDefinitionFunction.C1A5;
import io.cucumber.lambda.StepDefinitionFunction.C1A6;
import io.cucumber.lambda.StepDefinitionFunction.C1A7;
import io.cucumber.lambda.StepDefinitionFunction.C1A8;
import io.cucumber.lambda.StepDefinitionFunction.C1A9;
import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.List;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@API(status = EXPERIMENTAL, since = "7.4.0")
public final class StepDefinitions {

    private final List<StepDeclaration> stepDeclarations;
    StepDefinitions(List<StepDeclaration> stepDefinitions) {
        this.stepDeclarations = stepDefinitions;
    }

    List<StepDeclaration> getStepDeclarations() {
        return stepDeclarations;
    }

    public static <T> Builder<T> using(Class<T> context) {
        return new Builder<>(context);
    }

    public static final class Builder<Context> {

        private final List<StepDeclaration> stepDefinitions = new ArrayList<>();
        private final Class<Context> context;

        Builder(Class<Context> context) {
            this.context = context;
        }

        public StepDefinitions build() {
            return new StepDefinitions(stepDefinitions);
        }

        public Builder<Context> step(String expression, C1A0<Context> stepDefinitionFunction) {
            StackTraceElement location = new Exception().getStackTrace()[1];
            stepDefinitions.add(new StepDeclaration(expression, context, stepDefinitionFunction, location));
            return this;
        }

        public <P1> Builder<Context> step(String expression, C1A1<Context, P1> stepDefinitionFunction) {
            StackTraceElement location = new Exception().getStackTrace()[1];
            stepDefinitions.add(new StepDeclaration(expression, context, stepDefinitionFunction, location));
            return this;
        }

        public <P1, P2> Builder<Context> step(String expression, C1A2<Context, P1, P2> stepDefinitionFunction) {
            StackTraceElement location = new Exception().getStackTrace()[1];
            stepDefinitions.add(new StepDeclaration(expression, context, stepDefinitionFunction, location));
            return this;
        }

        public <P1, P2, P3> Builder<Context> step(
                String expression,
                C1A3<Context, P1, P2, P3> stepDefinitionFunction
        ) {
            StackTraceElement location = new Exception().getStackTrace()[1];
            stepDefinitions.add(new StepDeclaration(expression, context, stepDefinitionFunction, location));
            return this;
        }

        public <P1, P2, P3, P4> Builder<Context> step(
                String expression,
                C1A4<Context, P1, P2, P3, P4> stepDefinitionFunction
        ) {
            StackTraceElement location = new Exception().getStackTrace()[1];
            stepDefinitions.add(new StepDeclaration(expression, context, stepDefinitionFunction, location));
            return this;
        }

        public <P1, P2, P3, P4, P5> Builder<Context> step(
                String expression,
                C1A5<Context, P1, P2, P3, P4, P5> stepDefinitionFunction
        ) {
            StackTraceElement location = new Exception().getStackTrace()[1];
            stepDefinitions.add(new StepDeclaration(expression, context, stepDefinitionFunction, location));
            return this;
        }

        public <P1, P2, P3, P4, P5, P6> Builder<Context> step(
                String expression,
                C1A6<Context, P1, P2, P3, P4, P5, P6> stepDefinitionFunction
        ) {
            StackTraceElement location = new Exception().getStackTrace()[1];
            stepDefinitions.add(new StepDeclaration(expression, context, stepDefinitionFunction, location));
            return this;
        }

        public <P1, P2, P3, P4, P5, P6, P7> Builder<Context> step(
                String expression,
                C1A7<Context, P1, P2, P3, P4, P5, P6, P7> stepDefinitionFunction
        ) {
            StackTraceElement location = new Exception().getStackTrace()[1];
            stepDefinitions.add(new StepDeclaration(expression, context, stepDefinitionFunction, location));
            return this;
        }

        public <P1, P2, P3, P4, P5, P6, P7, P8> Builder<Context> step(
                String expression,
                C1A8<Context, P1, P2, P3, P4, P5, P6, P7, P8> stepDefinitionFunction
        ) {
            StackTraceElement location = new Exception().getStackTrace()[1];
            stepDefinitions.add(new StepDeclaration(expression, context, stepDefinitionFunction, location));
            return this;
        }

        public <P1, P2, P3, P4, P5, P6, P7, P8, P9> Builder<Context> step(
                String expression,
                C1A9<Context, P1, P2, P3, P4, P5, P6, P7, P8, P9> stepDefinitionFunction
        ) {
            StackTraceElement location = new Exception().getStackTrace()[1];
            stepDefinitions.add(new StepDeclaration(expression, context, stepDefinitionFunction, location));
            return this;
        }

    }

}
