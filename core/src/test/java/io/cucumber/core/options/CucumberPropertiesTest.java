package io.cucumber.core.options;

import io.cucumber.core.options.CucumberProperties.CucumberPropertiesMap;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

class CucumberPropertiesTest {

    @Test
    void looks_up_value_from_environment() {
        assertThat(CucumberProperties.fromEnvironment().get("PATH"), is(notNullValue()));
    }

    @Test
    void returns_null_for_absent_key() {
        CucumberPropertiesMap properties = new CucumberPropertiesMap(Collections.emptyMap());
        assertThat(properties.get("pxfj54#"), is(nullValue()));
    }

    @Test
    void returns_default_for_absent_key() {
        CucumberPropertiesMap properties = new CucumberPropertiesMap(Collections.emptyMap());
        assertThat(properties.getOrDefault("pxfj54#", "default"), is("default"));
    }

    @Test
    void looks_up_dotted_value_from_resource_bundle_with_dots() {
        Map<String, String> delegate = Collections.singletonMap("a.b", "a.b");
        CucumberPropertiesMap properties = new CucumberPropertiesMap(delegate);
        assertThat(properties.get("a.b"), is(equalTo("a.b")));
    }

    @Test
    void looks_up_underscored_value_from_resource_bundle_with_dots() {
        Map<String, String> delegate = Collections.singletonMap("B_C", "B_C");
        CucumberPropertiesMap properties = new CucumberPropertiesMap(delegate);
        assertThat(properties.get("b.c"), is(equalTo("B_C")));
    }

    @Test
    void looks_up_underscored_value_from_resource_bundle_with_underscores() {
        Map<String, String> delegate = Collections.singletonMap("B_C", "B_C");
        CucumberPropertiesMap properties = new CucumberPropertiesMap(delegate);
        assertThat(properties.get("B_C"), is(equalTo("B_C")));
    }

    @Test
    void looks_up_value_by_exact_case_key() {
        Map<String, String> delegate = Collections.singletonMap("c.D", "C_D");
        CucumberPropertiesMap properties = new CucumberPropertiesMap(delegate);
        assertThat(properties.get("c.D"), is(equalTo("C_D")));
    }

}
