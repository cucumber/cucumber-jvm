package io.cucumber.java;

import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.StepDefinition;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaStepDefinitionTransposeTest {

    @Test
    void transforms_to_map_of_double_to_double() throws Throwable {
        Method m = Steps.class.getMethod("mapOfDoubleToDouble", Map.class);
        assertFalse(isTransposed(m));
    }

    private boolean isTransposed(Method method) {
        Steps steps = new Steps();
        Lookup lookup = new SingletonFactory(steps);
        StepDefinition stepDefinition = new JavaStepDefinition(method, "some text", lookup);

        return stepDefinition.parameterInfos().get(0).isTransposed();
    }

    @Test
    void transforms_transposed_to_map_of_double_to_double() throws Throwable {
        Method m = Steps.class.getMethod("transposedMapOfDoubleToListOfDouble", Map.class);
        assertTrue(isTransposed(m));
    }

    public static class Steps {

        public void mapOfDoubleToDouble(Map<Double, Double> mapOfDoubleToDouble) {

        }

        public void transposedMapOfDoubleToListOfDouble(
                @Transpose Map<Double, List<Double>> mapOfDoubleToListOfDouble
        ) {
        }

    }

}
