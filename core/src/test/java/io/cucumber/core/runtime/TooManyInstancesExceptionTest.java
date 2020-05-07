package io.cucumber.core.runtime;

import io.cucumber.core.runtime.ScanningTypeRegistryConfigurerSupplier.TooManyInstancesException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

class TooManyInstancesExceptionTest {

    @Test
    void found_two_instances() {
        Collection<String> instances = Arrays.asList("one", "two");
        TooManyInstancesException expectedThrown = new TooManyInstancesException(instances);
        assertAll(
            () -> assertThat(expectedThrown.getMessage(),
                is(equalTo("Expected only one instance, but found too many: [one, two]"))),
            () -> assertThat(expectedThrown.getCause(), is(nullValue())));
    }

}
