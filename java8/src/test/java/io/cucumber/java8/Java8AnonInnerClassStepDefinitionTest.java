package io.cucumber.java8;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@SuppressWarnings("Convert2Lambda")
class Java8AnonInnerClassStepDefinitionTest {

    @Test
    void should_calculate_parameters_count_from_body_with_one_param() {
        Java8StepDefinition java8StepDefinition = Java8StepDefinition.create("I have some step",
            StepDefinitionBody.A1.class, oneParamStep());
        assertThat(java8StepDefinition.parameterInfos().size(), is(equalTo(1)));
    }

    private StepDefinitionBody.A1<?> oneParamStep() {
        return new StepDefinitionBody.A1<String>() {
            @Override
            public void accept(String p1) {
            }
        };
    }

    @Test
    void should_calculate_parameters_count_from_body_with_two_params() {
        Java8StepDefinition java8StepDefinition = Java8StepDefinition.create("I have some step",
            StepDefinitionBody.A2.class, twoParamStep());
        assertThat(java8StepDefinition.parameterInfos().size(), is(equalTo(2)));
    }

    private StepDefinitionBody.A2<?, ?> twoParamStep() {
        return new StepDefinitionBody.A2<String, String>() {
            @Override
            public void accept(String p1, String p2) {
            }
        };
    }

}
