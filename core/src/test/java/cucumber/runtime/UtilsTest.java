package cucumber.runtime;

import org.junit.Test;

import static cucumber.runtime.Utils.packageName;
import static cucumber.runtime.Utils.packagePath;
import static org.junit.Assert.assertEquals;

public class UtilsTest {
    @Test
    public void finds_path_for_class_in_package() {
        assertEquals("java.lang", packageName("java.lang.String"));
        assertEquals("java/lang", packagePath(String.class));
    }

    @Test
    public void finds_path_for_class_in_toplevel_package() {
        assertEquals("", packageName("TopLevelClass"));
        assertEquals("", packagePath(packageName("TopLevelClass")));
    }
}
