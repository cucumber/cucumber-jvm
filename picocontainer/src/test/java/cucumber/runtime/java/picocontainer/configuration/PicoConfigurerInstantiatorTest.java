package cucumber.runtime.java.picocontainer.configuration;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PicoConfigurerInstantiatorTest {

    private PicoConfigurerInstantiator instantiator = new PicoConfigurerInstantiator();

    @Test
    public void instantiates_configurer_by_fully_qualified_name() throws Exception {
        assertThat(instantiate(YourPicoConfigurer.class),
                is(instanceOf(YourPicoConfigurer.class)));
    }

    @Test(expected = PicoConfigurerInstantiationFailed.class)
    public void fails_to_instantiate_non_existant_class() throws Exception {
        instantiator.instantiate("some.bogus.Class");
    }

    @Test(expected = PicoConfigurerInstantiationFailed.class)
    public void fails_to_instantiate_class_not_implementing_pico_configurer() throws Exception {
        instantiate(String.class);
    }

    @Test(expected = PicoConfigurerInstantiationFailed.class)
    public void fails_to_instantiate_class_with_private_constructor() throws Exception {
        instantiate(PrivateConstructor.class);
    }

    private PicoConfigurer instantiate(Class<?> moduleClass) {
        String moduleClassName = moduleClass.getCanonicalName();
        return instantiator.instantiate(moduleClassName);
    }

}
