package io.cucumber.core.options;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;

public class CucumberPropertiesTest {

    private CucumberProperties.CucumberPropertiesMap env = new CucumberProperties.CucumberPropertiesMap(Collections.emptyMap());

    @BeforeEach
    public void setup() {
        env.put("ENV_TEST", "from-bundle");
        env.put("a.b", "a.b");
        env.put("B_C", "B_C");
        env.put("c.D", "C_D");
    }

    @Test
    public void looks_up_value_from_environment() {
        assertThat(CucumberProperties.fromEnvironment().get("PATH"), is(notNullValue()));
    }

    @Test
    public void returns_null_for_absent_key() {
        assertThat(env.get("pxfj54#"), is(nullValue()));
    }

    @Test
    public void looks_up_dotted_value_from_resource_bundle_with_dots() {
        assertEquals("a.b", env.get("a.b"));
    }

    @Test
    public void looks_up_underscored_value_from_resource_bundle_with_dots() {
        assertEquals("B_C", env.get("b.c"));
    }

    @Test
    public void looks_up_underscored_value_from_resource_bundle_with_underscores() {
        assertEquals("B_C", env.get("B_C"));
    }

    @Test
    public void looks_up_value_by_exact_case_key() {
        assertEquals("C_D", env.get("c.D"));
    }

}
