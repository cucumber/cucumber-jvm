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
    public void looks_up_value_from_system_properties() {
        try {
            System.setProperty("EnvTest", "from-props");
            assertEquals("from-props", env.get("EnvTest"));
        } finally {
            System.getProperties().remove("EnvTest");
        }
    }

    @Test
    public void looks_up_value_from_resource_bundle() {
        assertEquals("from-bundle", env.get("EnvTest"));
    }
}
