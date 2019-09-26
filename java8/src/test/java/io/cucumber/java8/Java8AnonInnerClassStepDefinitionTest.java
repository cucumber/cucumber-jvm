package io.cucumber.java8;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@SuppressWarnings("Convert2Lambda")
class Java8AnonInnerClassStepDefinitionTest {

    @Test
    void should_calculate_parameters_count_from_body_with_one_param() {
        Java8StepDefinition java8StepDefinition = Java8StepDefinition.create("I have some step", StepDefinitionBody.A1.class, oneParamStep());
        assertThat(java8StepDefinition.parameterInfos().size(), is(equalTo(1)));
    }

    @Test
    void should_calculate_parameters_count_from_body_with_two_params() {
        Java8StepDefinition java8StepDefinition = Java8StepDefinition.create("I have some step", StepDefinitionBody.A2.class, twoParamStep());
        assertThat(java8StepDefinition.parameterInfos().size(), is(equalTo(2)));
    }

    private StepDefinitionBody.A1 oneParamStep() {
        return new StepDefinitionBody.A1<String>() {
            @Override
            public void accept(String p1) {
            }
        };
    }

    private StepDefinitionBody.A2 twoParamStep() {
        return new StepDefinitionBody.A2<String, String>() {
            @Override
            public void accept(String p1, String p2) {
            }
        };
    }

    private StepDefinitionBody.A1 genericListStep() {
        return new StepDefinitionBody.A1<List<String>>() {
            @Override
            public void accept(List<String> p1) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private StepDefinitionBody.A1 nonGenericListStep() {
        return new StepDefinitionBody.A1<List>() {
            @Override
            public void accept(List p1) {
                throw new UnsupportedOperationException();
            }
        };
    }

}
