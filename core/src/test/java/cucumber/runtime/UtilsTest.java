package cucumber.runtime;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static cucumber.runtime.Utils.isInstantiable;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UtilsTest {
    @Test
    public void public_non_static_inner_classes_are_not_instantiable() {
        assertFalse(isInstantiable(NonStaticInnerClass.class));
    }

    @Test
    public void public_static_inner_classes_are_instantiable() {
        assertTrue(isInstantiable(StaticInnerClass.class));
    }

    public class NonStaticInnerClass {
    }

    public static class StaticInnerClass {
    }
}
