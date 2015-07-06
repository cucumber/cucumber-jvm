package cucumber.runtime;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class EnvTest {

    private Env env = new Env("env-test");

    @Test
    public void looks_up_value_from_environment() {
        assertNotNull(env.get("PATH"));
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
    public void looks_up_dotted_value_from_resource_bundle_with_underscores() {
        assertEquals("a.b", env.get("A_B"));
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
    public void looks_up_value_by_exact_case_keuy() {
        assertEquals("C_D", env.get("c.D"));
    }
}
