package io.cucumber.core.options;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CucumberPropertiesTest {

    private CucumberProperties.CucumberPropertiesMap env = new CucumberProperties.CucumberPropertiesMap(Collections.emptyMap());

    @Before
    public void setup(){
        env.put("ENV_TEST","from-bundle");
        env.put("a.b","a.b");
        env.put("B_C","B_C");
        env.put("c.D","C_D");
    }

    @Test
    public void looks_up_value_from_environment() {
        assertNotNull(CucumberProperties.fromEnvironment().get("PATH"));
    }

    @Test
    public void returns_null_for_absent_key() {
        assertNull(env.get("pxfj54#"));
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
