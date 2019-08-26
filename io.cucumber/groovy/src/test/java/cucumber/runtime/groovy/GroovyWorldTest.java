package cucumber.runtime.groovy;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GroovyWorldTest  {
    GroovyWorld world;

    @Before
    public void setUp() {
       world = new GroovyWorld();
    }

    @Test(expected = RuntimeException.class)
    public void should_not_register_pure_java_object() {
        world.registerWorld("JAVA");
    }

    @Test
    public void should_support_more_then_one_World() {
        world.registerWorld(new CustomWorld());
        world.registerWorld(new AnotherCustomWorld());

        world.setProperty("lastAte", "groovy");
        assertEquals("groovy", world.getProperty("lastAte"));

        world.setProperty("aProperty", 1);
        assertEquals(1, world.getProperty("aProperty"));

        List<Integer> intArgs = Arrays.asList(1,2);
        world.invokeMethod("aMethod", intArgs);
        assertEquals(intArgs, world.getProperty("methodArgs"));

        world.invokeMethod("aMethod", null);
        assertEquals("no args", world.getProperty("methodArgs"));
    }

    @Test(expected = RuntimeException.class)
    public void should_detect_double_property_definition() {
        world.registerWorld(new WorldWithPropertyAndMethod());
        world.registerWorld(new AnotherCustomWorld());

        world.getProperty("aProperty");
    }

    @Test(expected = RuntimeException.class)
    public void should_detect_double_method_definition() {
        world.registerWorld(new WorldWithPropertyAndMethod());
        world.registerWorld(new AnotherCustomWorld());

        world.invokeMethod("aMethod", new Integer[]{1,2});
    }
}
