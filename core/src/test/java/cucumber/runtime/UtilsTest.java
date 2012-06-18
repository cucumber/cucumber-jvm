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

    @Test
    public void doesnt_time_out_if_it_takes_too_long() throws Throwable {
        Slow slow = new Slow();
        Object what = Utils.invoke(slow, Slow.class.getMethod("slow"), 50);
        assertEquals("slow", what);
    }

    @Test(expected = TimeoutException.class)
    public void times_out_if_it_takes_too_long() throws Throwable {
        Slow slow = new Slow();
        Utils.invoke(slow, Slow.class.getMethod("slower"), 50);
        fail();
    }

    public static class Slow {
        public String slow() throws InterruptedException {
            sleep(10);
            return "slow";
        }

        public String slower() throws InterruptedException {
            sleep(100);
            return "slower";
        }
    }
}
