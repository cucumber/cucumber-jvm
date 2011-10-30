package cucumber.junit;

import org.junit.Test;

import static cucumber.junit.Cucumber.packageName;
import static org.junit.Assert.assertEquals;

public class CucumberTest {
    @Test
    public void finds_path_for_class_in_package() {
        assertEquals("java.lang", packageName("java.lang.String"));
    }

    @Test
    public void finds_path_for_class_in_toplevel_package() {
        assertEquals("", packageName("TopLevelClass"));
    }
}
