package io.cucumber.core.options;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

 class CucumberPropertiesTest {

    private CucumberProperties.CucumberPropertiesMap env = new CucumberProperties.CucumberPropertiesMap(Collections.emptyMap());

    @BeforeEach
     void setup() {
        env.put("ENV_TEST", "from-bundle");
        env.put("a.b", "a.b");
        env.put("B_C", "B_C");
        env.put("c.D", "C_D");
    }

    @Test
     void looks_up_value_from_environment() {
        assertThat(CucumberProperties.fromEnvironment().get("PATH"), is(notNullValue()));
    }

    @Test
     void returns_null_for_absent_key() {
        assertThat(env.get("pxfj54#"), is(nullValue()));
    }

    @Test
     void looks_up_dotted_value_from_resource_bundle_with_dots() {
        assertThat(env.get("a.b"), is(equalTo("a.b")));
    }

    @Test
     void looks_up_underscored_value_from_resource_bundle_with_dots() {
        assertThat(env.get("b.c"), is(equalTo("B_C")));
    }

    @Test
     void looks_up_underscored_value_from_resource_bundle_with_underscores() {
        assertThat(env.get("B_C"), is(equalTo("B_C")));
    }

    @Test
     void looks_up_value_by_exact_case_key() {
        assertThat(env.get("c.D"), is(equalTo("C_D")));
    }

}
