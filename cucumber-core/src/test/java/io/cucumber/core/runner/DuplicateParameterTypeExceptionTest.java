package io.cucumber.core.runner;

import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.cucumberexpressions.ParameterType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("NullAway")
class DuplicateParameterTypeExceptionTest {

    @Test
    void can_report_duplicate_parameter_types() {
        ParameterTypeDefinition a = new StubParameterTypeDefinition("iso8601Date", "ParameterTypeA_Location");
        ParameterTypeDefinition b = new StubParameterTypeDefinition("iso8601Date", "ParameterTypeB_Location");

        DuplicateParameterTypeException expectedThrown = new DuplicateParameterTypeException(a, b);
        assertAll(
            () -> assertThat(expectedThrown.getMessage(),
                is(equalTo(
                    "Duplicate parameter type 'iso8601Date' in ParameterTypeA_Location and ParameterTypeB_Location"))),
            () -> assertThat(expectedThrown.getCause(), is(nullValue())));
    }

    private static final class StubParameterTypeDefinition implements ParameterTypeDefinition {

        private final String name;
        private final String location;

        private StubParameterTypeDefinition(String name, String location) {
            this.name = name;
            this.location = location;
        }

        @Override
        public ParameterType<?> parameterType() {
            return new ParameterType<>(name, "[ab]", Object.class, (String arg) -> new Object());
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return location;
        }

    }

}
