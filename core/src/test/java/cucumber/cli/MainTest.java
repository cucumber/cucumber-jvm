package cucumber.cli;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MainTest {
    @Test
    public void has_version_from_properties_file() {
        assertTrue(Main.VERSION.startsWith("1.0"));
    }

}
