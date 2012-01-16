package cucumber.cli;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainTest {
    @Test
    public void has_version_from_properties_file() {
        assertEquals("1.0.0.RC4", Main.VERSION);
    }

}
