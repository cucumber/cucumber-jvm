package cucumber.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cucumber.runtime.RuntimeOptions;

public class RuntimeOptionsFactoryTest
{
    @Test
    public void create_strict() throws Exception
    {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(Strict.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertTrue(runtimeOptions.strict);
    }

    @Test
    public void create_non_strict() throws Exception
    {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(NotStrict.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertFalse(runtimeOptions.strict);
    }

    @Test
    public void create_without_options() throws Exception
    {
        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(WithoutOptions.class);
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();
        assertFalse(runtimeOptions.strict);
    }

    @Cucumber.Options(strict=true)
    public static class Strict {
        // empty
    }

    @Cucumber.Options()
    public static class NotStrict {
        // empty
    }

    public static class WithoutOptions {
        // empty
    }

}
